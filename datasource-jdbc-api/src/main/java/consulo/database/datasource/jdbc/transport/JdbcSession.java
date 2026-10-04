/*
 * Copyright 2013-2020 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.database.datasource.jdbc.transport;

import consulo.application.progress.ProgressIndicator;
import consulo.application.util.concurrent.AppExecutorUtil;
import consulo.component.ProcessCanceledException;
import consulo.container.plugin.PluginManager;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.configurable.SecureString;
import consulo.database.datasource.driver.DataSourceDriver;
import consulo.database.datasource.driver.DataSourceDriverRegistry;
import consulo.database.datasource.driver.DataSourceDriverStart;
import consulo.database.datasource.jdbc.provider.JdbcDataSourceProvider;
import consulo.database.datasource.localize.DataSourceLocalize;
import consulo.database.datasource.model.DataSource;
import consulo.database.jdbc.rt.shared.JdbcExecutor;
import consulo.platform.Platform;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.cmd.SimpleJavaParameters;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.util.dataholder.Key;
import consulo.util.io.ClassPathUtil;
import consulo.util.io.NetUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ref.SimpleReference;
import jakarta.annotation.Nonnull;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author VISTALL
 * @since 2020-08-18
 */
public class JdbcSession implements AutoCloseable {
    private enum SessionState {
        UNDEFINED,
        WAIT_CLIENT,
        CANCELED,
        ERROR
    }

    /**
     * The only start this session runs: the JDBC runtime module of the plugin in a JVM, with the driver files on its class path.
     */
    private static final DataSourceDriverStart DRIVER_START = new DataSourceDriverStart(DataSourceDriverStart.JAVA_RT, "jdbc");

    private ProcessHandler myProcessHandler;

    private Future<?> myWatcherTask = CompletableFuture.completedFuture(null);

    private JdbcExecutor.Client myClient;

    private final TSocket mySocket;

    private SessionState myState = SessionState.UNDEFINED;

    private final CountDownLatch myCountDownLatch = new CountDownLatch(1);

    private final int myPort;

    public JdbcSession(@Nonnull ProgressIndicator indicator, @Nonnull DataSource dataSource) throws Exception {
        JdbcDataSourceProvider provider = (JdbcDataSourceProvider) dataSource.getProvider();

        DataSourceDriver driver = DataSourceDriverRegistry.getInstance().resolve(indicator, dataSource);
        if (!DRIVER_START.equals(driver.start())) {
            throw new ExecutionException(DataSourceLocalize.errorDriverStartUnsupported(driver.name(),
                driver.version(),
                driver.start().kind(),
                driver.start().agent()));
        }

        String jdbcUrl = provider.buildJdbcUrl(dataSource);

        myPort = NetUtil.findAvailableSocketPort();

        String login = dataSource.getProperties().get(GenericPropertyKeys.LOGIN);
        SecureString password = dataSource.getProperties().get(GenericPropertyKeys.PASSWORD);

        // a value which is not set gets no key: the protocol can not send null, and the driver then uses its own default
        Map<String, String> properties = new HashMap<>();
        if (!StringUtil.isEmpty(login)) {
            properties.put("user", login);
        }

        String passwordValue = password == null ? null : password.getValue(dataSource);
        if (!StringUtil.isEmpty(passwordValue)) {
            properties.put("password", passwordValue);
        }

        SimpleReference<Integer> exitCodeRef = SimpleReference.create();

        Platform platform = Platform.current();

        String java_home = platform.jvm().getRuntimeProperty("java.home");

        SimpleJavaParameters simpleJavaParameters = new SimpleJavaParameters();
        simpleJavaParameters.getClassPath().add(new File(PluginManager.getPluginPath(DefaultJdbcDataSourceTransport.class), "rt/consulo.database-datasource.jdbc.rt.jar"));
        simpleJavaParameters.getClassPath().add(ClassPathUtil.getJarPathForClass(JdbcExecutor.class));
        simpleJavaParameters.getClassPath().add(ClassPathUtil.getJarPathForClass(TServer.class));
        simpleJavaParameters.getClassPath().add(ClassPathUtil.getJarPathForClass(Logger.class));
        for (Path file : driver.files()) {
            simpleJavaParameters.getClassPath().add(file.toFile());
        }
        simpleJavaParameters.setMainClass("consulo.database.jdbc.rt.Main");
        simpleJavaParameters.setJdkHome(java_home);
        simpleJavaParameters.getProgramParametersList().add(String.valueOf(myPort));

        String userTimeZone = ZoneId.systemDefault().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ROOT);

        simpleJavaParameters.getVMParametersList().add("-Duser.timezone=" + userTimeZone);

        ProcessHandler processHandler = simpleJavaParameters.createProcessHandler();
        myProcessHandler = processHandler;

        processHandler.addProcessListener(new ProcessListener() {
            @Override
            public void onTextAvailable(ProcessEvent event, Key outputType) {
                if (event.getText().trim().equals("[binded]")) {
                    changeState(SessionState.WAIT_CLIENT);
                }
            }

            @Override
            public void processTerminated(ProcessEvent event) {
                exitCodeRef.setIfNull(event.getExitCode());

                changeState(SessionState.ERROR);
            }
        });

        myWatcherTask = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> {
            // process terminated
            if (exitCodeRef.get() != null) {
                myWatcherTask.cancel(false);
                return;
            }

            if (indicator.isCanceled()) {
                // force set canceled
                myState = SessionState.CANCELED;

                ProcessHandler handler = myProcessHandler;
                if (handler != null) {
                    // reset ref - no need kill process twice
                    myProcessHandler = null;
                    handler.destroyProcess();
                }
            }

        }, 500, 500, TimeUnit.MILLISECONDS);

        processHandler.startNotify();

        myCountDownLatch.await();

        if (myState == SessionState.CANCELED) {
            closeImpl();

            throw new ProcessCanceledException();
        }

        if (myState != SessionState.WAIT_CLIENT) {
            closeImpl();

            throw new Exception("Connection error");
        }

        try {
            mySocket = new TSocket("localhost", myPort);

            mySocket.open();

            myClient = new JdbcExecutor.Client(new TBinaryProtocol(mySocket));

            myClient.connect(jdbcUrl, properties);
        }
        catch (Exception e) {
            closeImpl();

            throw e;
        }
    }

    private void changeState(SessionState state) {
        if (myState == SessionState.UNDEFINED) {
            myState = state;
            myCountDownLatch.countDown();
        }
    }

    public <T> T execute(@Nonnull JdbcExecutorAction<T> action) throws TException {
        assert myClient != null;
        try {
            return action.run(myClient);
        }
        catch (TException e) {
            if (myState == SessionState.CANCELED) {
                closeImpl();

                throw new ProcessCanceledException();
            }

            closeImpl();

            throw e;
        }
    }

    @Override
    public void close() throws Exception {
        closeImpl();
    }

    private void closeImpl() {
        try {
            if (mySocket != null) {
                mySocket.close();
            }
        }
        catch (Exception ignored) {
        }

        if (myProcessHandler != null) {
            myProcessHandler.destroyProcess();
            myProcessHandler = null;
        }

        myWatcherTask.cancel(false);
    }
}
