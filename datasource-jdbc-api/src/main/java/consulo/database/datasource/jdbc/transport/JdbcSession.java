package consulo.database.datasource.jdbc.transport;

import com.intellij.execution.configurations.SimpleJavaParameters;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.PathUtil;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.DownloadUtil;
import consulo.container.boot.ContainerPathManager;
import consulo.container.plugin.PluginManager;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.jdbc.provider.JdbcDataSourceProvider;
import consulo.database.datasource.model.DataSource;
import consulo.database.jdbc.rt.shared.JdbcExecutor;
import consulo.platform.Platform;
import consulo.util.dataholder.Key;
import consulo.util.lang.ref.SimpleReference;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TSocket;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author VISTALL
 * @since 2020-08-18
 */
public class JdbcSession implements AutoCloseable
{
	private enum SessionState
	{
		UNDEFINED,
		WAIT_CLIENT,
		CANCELED,
		ERROR
	}

	private ProcessHandler myProcessHandler;

	private Future<?> myWatcherTask = CompletableFuture.completedFuture(null);

	private JdbcExecutor.Client myClient;

	private final TSocket mySocket;

	private SessionState myState = SessionState.UNDEFINED;

	private final CountDownLatch myCountDownLatch = new CountDownLatch(1);

	public JdbcSession(@Nonnull ProgressIndicator indicator, @Nonnull DataSource dataSource) throws Exception
	{
		JdbcDataSourceProvider provider = (JdbcDataSourceProvider) dataSource.getProvider();

		Path driverPath = prepareJdbcDriver(indicator, provider);

		String jdbcUrl = provider.buildJdbcUrl(dataSource);

		String login = dataSource.getProperties().get(GenericPropertyKeys.LOGIN);
		String password = dataSource.getProperties().get(GenericPropertyKeys.PASSWORD);

		Map<String, String> properties = new HashMap<>();
		properties.put("user", login);
		properties.put("password", password);

		SimpleReference<Integer> exitCodeRef = SimpleReference.create();

		Platform platform = Platform.current();

		String java_home = platform.os().getEnvironmentVariable("JAVA_HOME");

		SimpleJavaParameters simpleJavaParameters = new SimpleJavaParameters();
		simpleJavaParameters.getClassPath().add(new File(PluginManager.getPluginPath(DefaultJdbcDataSourceTransport.class), "rt/consulo.database-datasource.jdbc.rt.jar"));
		simpleJavaParameters.getClassPath().add(PathUtil.getJarPathForClass(JdbcExecutor.class));
		simpleJavaParameters.getClassPath().add(PathUtil.getJarPathForClass(TServer.class));
		simpleJavaParameters.getClassPath().add(PathUtil.getJarPathForClass(org.slf4j.Logger.class));
		simpleJavaParameters.getClassPath().add(driverPath.toFile());
		simpleJavaParameters.setMainClass("consulo.database.jdbc.rt.Main");
		simpleJavaParameters.setJdk(new FakeSdk(java_home));

		OSProcessHandler processHandler = simpleJavaParameters.createOSProcessHandler();
		myProcessHandler = processHandler;

		processHandler.addProcessListener(new ProcessAdapter()
		{
			@Override
			public void onTextAvailable(ProcessEvent event, Key outputType)
			{
				if(event.getText().trim().equals("[binded]"))
				{
					changeState(SessionState.WAIT_CLIENT);
				}
			}

			@Override
			public void processTerminated(ProcessEvent event)
			{
				exitCodeRef.setIfNull(event.getExitCode());

				changeState(SessionState.ERROR);
			}
		});

		myWatcherTask = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() ->
		{
			// process terminated
			if(exitCodeRef.get() != null)
			{
				myWatcherTask.cancel(false);
				return;
			}

			if(indicator.isCanceled())
			{
				// force set canceled
				myState = SessionState.CANCELED;

				ProcessHandler handler = myProcessHandler;
				if(handler != null)
				{
					// reset ref - no need kill process twice
					myProcessHandler = null;
					handler.destroyProcess();
				}
			}

		}, 500, 500, TimeUnit.MILLISECONDS);

		processHandler.startNotify();

		myCountDownLatch.await();

		if(myState == SessionState.CANCELED)
		{
			closeImpl();

			throw new ProcessCanceledException();
		}

		if(myState != SessionState.WAIT_CLIENT)
		{
			closeImpl();

			throw new Exception("Connection error");
		}

		try
		{
			mySocket = new TSocket("localhost", 6645);

			mySocket.open();

			myClient = new JdbcExecutor.Client(new TBinaryProtocol(mySocket));

			myClient.connect(jdbcUrl, properties);
		}
		catch(Exception e)
		{
			closeImpl();

			throw e;
		}
	}

	private void changeState(SessionState state)
	{
		if(myState == SessionState.UNDEFINED)
		{
			myState = state;
			myCountDownLatch.countDown();
		}
	}

	public <T> T execute(@Nonnull JdbcExecutorAction<T> action) throws TException
	{
		assert myClient != null;
		try
		{
			return action.run(myClient);
		}
		catch(TException e)
		{
			if(myState == SessionState.CANCELED)
			{
				closeImpl();

				throw new ProcessCanceledException();
			}

			closeImpl();

			throw e;
		}
	}

	@Nonnull
	private Path prepareJdbcDriver(@Nonnull ProgressIndicator indicator, @Nonnull JdbcDataSourceProvider provider) throws IOException
	{
		LinkedHashMap<String, String> drivers = new LinkedHashMap<>();

		provider.fillDrivers(drivers);

		Map.Entry<String, String> selectedDriver = ContainerUtil.getFirstItem(drivers.entrySet());

		Path driverPath = Paths.get(ContainerPathManager.get().getSystemPath(), "datasource-drivers", provider.getId(), selectedDriver.getKey());

		if(!Files.exists(driverPath))
		{
			String url = selectedDriver.getValue();

			Files.createDirectories(driverPath.getParent());

			DownloadUtil.downloadContentToFile(indicator, url, driverPath.toFile());
		}

		return driverPath;
	}

	@Override
	public void close() throws Exception
	{
		closeImpl();
	}

	private void closeImpl()
	{
		try
		{
			if(mySocket != null)
			{
				mySocket.close();
			}
		}
		catch(Exception ignored)
		{
		}

		if(myProcessHandler != null)
		{
			myProcessHandler.destroyProcess();
			myProcessHandler = null;
		}

		myWatcherTask.cancel(false);
	}
}
