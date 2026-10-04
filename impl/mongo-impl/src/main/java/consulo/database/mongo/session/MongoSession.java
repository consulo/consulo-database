/*
 * Copyright 2013-2026 consulo.io
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


package consulo.database.mongo.session;

import consulo.application.progress.ProgressIndicator;
import consulo.component.ProcessCanceledException;
import consulo.container.plugin.PluginManager;
import consulo.database.datasource.driver.DataSourceDriver;
import consulo.database.datasource.driver.DataSourceDriverRegistry;
import consulo.database.datasource.driver.DataSourceDriverStart;
import consulo.database.datasource.localize.DataSourceLocalize;
import consulo.database.datasource.model.DataSource;
import consulo.database.mongo.localize.MongoLocalize;
import consulo.database.mongo.rt.shared.MongoExecutor;
import consulo.database.mongo.rt.shared.MongoExecutorConstants;
import consulo.database.mongo.rt.shared.MongoFailError;
import consulo.database.mongo.rt.shared.MongoHelloResult;
import consulo.logging.Logger;
import consulo.platform.Platform;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessOutputType;
import consulo.process.cmd.SimpleJavaParameters;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.util.dataholder.Key;
import consulo.util.io.ClassPathUtil;
import org.apache.thrift.TConfiguration;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TSocket;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * One MongoDB runtime process: a JVM which runs the MongoDB driver and answers over Thrift, like the JDBC runtime. The IDE side never
 * loads a driver or BSON class.
 * <p>
 * Starting: the driver of the data source is resolved by the {@link DataSourceDriverRegistry} - downloaded and verified when needed -
 * and the runtime jar of the plugin is started with the driver files on its class path, and two random tokens are written to its
 * stdin - never to the command line or the environment. The runtime binds an ephemeral port on {@code 127.0.0.1} and prints it on
 * its ready line. Every connection starts with {@code hello(clientToken)}, which proves the IDE to the runtime; the answer carries the
 * server token, which proves the runtime to the IDE before anything else - the settings with the password in particular - is sent.
 * stdin stays open for the life of the process: when it closes, because the session is closed or the IDE died, the runtime exits.
 * <p>
 * Calls run on pooled connections, so a tree refresh and a fetch can run together. A cancelled indicator aborts the socket of its
 * call, which fails the blocked read at once.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public final class MongoSession implements AutoCloseable {
    private static final Logger LOG = Logger.getInstance(MongoSession.class);

    /**
     * The runtime jar inside the plugin directory, copied there by the plugin packaging.
     */
    static final String RUNTIME_JAR = "rt/consulo.database-datasource.mongo.rt.jar";

    /**
     * The only start this session runs: the MongoDB runtime module of the plugin in a JVM, with the driver files on its class path.
     */
    private static final DataSourceDriverStart DRIVER_START = new DataSourceDriverStart(DataSourceDriverStart.JAVA_RT, "mongo");

    private static final long START_TIMEOUT_MS = 60_000;
    private static final long POLL_MS = 100;

    /**
     * Above the worst case of a call: 15 s server selection, 30 s waiting for a free query slot of the runtime and 30 s maxTime.
     */
    private static final int SOCKET_TIMEOUT_MS = 120_000;
    private static final int CONNECT_TIMEOUT_MS = 10_000;

    /**
     * libthrift 0.21 enforces neither limit on a socket - the response budgets of the runtime bound an answer. They are set for a
     * later libthrift which may: 256 MiB covers a full find answer, and BSON nests at most about 200 levels.
     */
    private static final int MAX_MESSAGE_SIZE = 256 * 1024 * 1024;
    private static final int RECURSION_LIMIT = 512;

    private static final int MAX_IDLE_CONNECTIONS = 4;
    private static final long SHUTDOWN_WAIT_MS = 2_000;
    private static final int TOKEN_BYTES = 32;
    private static final int MAX_BUFFERED_OUTPUT = 8 * 1024;

    /**
     * A small heap which grows only when an answer needs it - about 67 MB resident when idle.
     */
    private static final List<String> VM_OPTIONS = List.of("-Xms16m",
        "-Xmx1g",
        "-XX:+UseSerialGC",
        "-XX:TieredStopAtLevel=1",
        "-XX:ReservedCodeCacheSize=32m",
        "-Xss512k",
        "-Djava.awt.headless=true");

    private static final SecureRandom RANDOM = new SecureRandom();

    private final ProcessHandler myProcessHandler;
    private final OutputStream myProcessInput;
    private final ScheduledExecutorService myScheduler;
    private final int myPort;
    private final String myClientToken;
    private final String myServerToken;

    private final Deque<MongoConnection> myIdleConnections = new ArrayDeque<>();

    private final Object myStateLock = new Object();
    private int myActiveCalls;
    private long myLastUsedNanos = System.nanoTime();
    private volatile boolean myRetiring;
    private volatile boolean myClosing;
    private @Nullable Executor myRetireExecutor;

    private MongoSession(ProcessHandler processHandler,
                         OutputStream processInput,
                         ScheduledExecutorService scheduler,
                         int port,
                         String clientToken,
                         String serverToken) {
        myProcessHandler = processHandler;
        myProcessInput = processInput;
        myScheduler = scheduler;
        myPort = port;
        myClientToken = clientToken;
        myServerToken = serverToken;
    }

    /**
     * Starts a runtime process and waits until it answers the handshake. It has no MongoDB client yet: {@code connect} or
     * {@code testConnection} comes next.
     *
     * @param scheduler runs the watchers which abort the calls of a cancelled indicator
     */
    public static MongoSession start(ProgressIndicator indicator,
                                     DataSource dataSource,
                                     ScheduledExecutorService scheduler) throws IOException, ExecutionException {
        indicator.checkCanceled();

        DataSourceDriver driver = DataSourceDriverRegistry.getInstance().resolve(indicator, dataSource);
        if (!DRIVER_START.equals(driver.start())) {
            throw new MongoRuntimeException(DataSourceLocalize.errorDriverStartUnsupported(driver.name(),
                driver.version(),
                driver.start().kind(),
                driver.start().agent()));
        }

        File runtimeJar = new File(PluginManager.getPluginPath(MongoSession.class), RUNTIME_JAR);
        if (!runtimeJar.isFile()) {
            throw new MongoRuntimeException(MongoLocalize.errorRuntimeMissing(runtimeJar.getPath()));
        }

        indicator.checkCanceled();
        indicator.setText(MongoLocalize.progressStartingRuntime());

        SimpleJavaParameters parameters = new SimpleJavaParameters();
        parameters.setJdkHome(Platform.current().jvm().getRuntimeProperty("java.home"));
        parameters.getClassPath().add(runtimeJar);
        parameters.getClassPath().add(ClassPathUtil.getJarPathForClass(MongoExecutor.class));
        parameters.getClassPath().add(ClassPathUtil.getJarPathForClass(TServer.class));
        parameters.getClassPath().add(ClassPathUtil.getJarPathForClass(org.slf4j.Logger.class));
        for (Path file : driver.files()) {
            parameters.getClassPath().add(file.toFile());
        }
        parameters.setMainClass(MongoExecutorConstants.MAIN_CLASS);
        // no program parameters and no environment: the tokens go to stdin, the settings over the authenticated connection
        parameters.getVMParametersList().addAll(VM_OPTIONS);

        ProcessHandler processHandler = parameters.createProcessHandler();
        RuntimeOutputListener listener = new RuntimeOutputListener();
        processHandler.addProcessListener(listener);

        OutputStream processInput = processHandler.getProcessInput();
        boolean started = false;
        try {
            if (processInput == null) {
                throw new IOException("The MongoDB runtime process has no input stream");
            }

            String clientToken = newToken();
            String serverToken = newToken();
            processInput.write((clientToken + "\n" + serverToken + "\n").getBytes(StandardCharsets.UTF_8));
            processInput.flush();

            processHandler.startNotify();

            int port = awaitPort(indicator, listener.getPort());

            MongoSession session = new MongoSession(processHandler, processInput, scheduler, port, clientToken, serverToken);
            // the first connection proves the runtime before a caller sends anything; it is pooled for the first call
            session.releaseConnection(session.openConnection());
            started = true;
            return session;
        }
        finally {
            if (!started) {
                closeQuietly(processInput);
                processHandler.destroyProcess();
            }
        }
    }

    private static int awaitPort(ProgressIndicator indicator, CompletableFuture<Integer> port) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(START_TIMEOUT_MS);
        while (true) {
            indicator.checkCanceled();
            try {
                return port.get(POLL_MS, TimeUnit.MILLISECONDS);
            }
            catch (TimeoutException e) {
                if (System.nanoTime() - deadline > 0) {
                    throw new MongoRuntimeException(MongoLocalize.errorRuntimeStartTimeout());
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ProcessCanceledException();
            }
            catch (java.util.concurrent.ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new IllegalStateException(cause);
            }
        }
    }

    /**
     * Runs a call on a pooled connection. Must be called off the UI thread.
     *
     * @throws MongoRuntimeException     the runtime reported an error, or the connection to it broke
     * @throws ProcessCanceledException the indicator was cancelled - the session stays usable
     */
    public <T extends @Nullable Object> T execute(ProgressIndicator indicator, MongoCall<T> call) {
        indicator.checkCanceled();

        enterCall();
        try {
            MongoConnection connection = borrowConnection();
            long callId = connection.beginCall();
            ScheduledFuture<?> watcher = myScheduler.scheduleWithFixedDelay(() -> {
                if (indicator.isCanceled()) {
                    connection.abort(callId);
                }
            }, POLL_MS, POLL_MS, TimeUnit.MILLISECONDS);

            boolean reusable = false;
            try {
                T value = call.run(connection.getClient());
                reusable = true;
                return value;
            }
            catch (MongoFailError e) {
                // a declared error is a normal answer: the connection is fine
                reusable = true;
                throw MongoRuntimeException.of(e);
            }
            catch (TException e) {
                if (indicator.isCanceled()) {
                    throw new ProcessCanceledException();
                }
                // the process may have died - isAlive() tells
                throw MongoRuntimeException.lost(e);
            }
            finally {
                watcher.cancel(false);
                // decided under the monitor of the connection, so a late tick of the watcher cannot abort a pooled connection
                boolean intact = connection.endCall();
                if (reusable && intact) {
                    releaseConnection(connection);
                }
                else {
                    connection.close();
                }
            }
        }
        finally {
            exitCall();
        }
    }

    /**
     * @return whether the process runs and the session is neither closed nor retired
     */
    public boolean isAlive() {
        return !myClosing && !myRetiring && !myProcessHandler.isProcessTerminating() && !myProcessHandler.isProcessTerminated();
    }

    /**
     * Marks the session as used now, which restarts its idle time.
     */
    public void touch() {
        synchronized (myStateLock) {
            myLastUsedNanos = System.nanoTime();
        }
    }

    public int getActiveCalls() {
        synchronized (myStateLock) {
            return myActiveCalls;
        }
    }

    /**
     * @return whether no call runs and none ran for the given time
     */
    public boolean isIdleFor(long nanos) {
        synchronized (myStateLock) {
            return myActiveCalls == 0 && System.nanoTime() - myLastUsedNanos >= nanos;
        }
    }

    /**
     * Closes the session once its running calls are done - at once when none runs. A call which starts while others still run is
     * served; once the session closes, every call fails. Closing runs on the executor, as it waits for the process to exit.
     */
    public void closeWhenIdle(Executor executor) {
        boolean closeNow;
        synchronized (myStateLock) {
            if (myClosing || myRetiring) {
                return;
            }

            myRetiring = true;
            myRetireExecutor = executor;
            closeNow = myActiveCalls == 0;
            if (closeNow) {
                myClosing = true;
            }
        }

        if (closeNow) {
            executor.execute(this::doClose);
        }
    }

    /**
     * Stops the process: asks it to shut down, closes its stdin and kills it when it has not exited after two seconds. It blocks for
     * that long at most, so it must not run on the UI thread.
     */
    @Override
    public void close() {
        synchronized (myStateLock) {
            if (myClosing) {
                return;
            }
            myClosing = true;
        }
        doClose();
    }

    private void enterCall() {
        synchronized (myStateLock) {
            if (myClosing) {
                throw new MongoRuntimeException(MongoLocalize.errorRuntimeStopped());
            }

            myActiveCalls++;
            myLastUsedNanos = System.nanoTime();
        }
    }

    private void exitCall() {
        Executor closeExecutor = null;
        synchronized (myStateLock) {
            myActiveCalls--;
            myLastUsedNanos = System.nanoTime();

            if (myRetiring && !myClosing && myActiveCalls == 0) {
                myClosing = true;
                closeExecutor = myRetireExecutor;
            }
        }

        if (closeExecutor != null) {
            closeExecutor.execute(this::doClose);
        }
    }

    private void doClose() {
        List<MongoConnection> connections;
        synchronized (myIdleConnections) {
            connections = new ArrayList<>(myIdleConnections);
            myIdleConnections.clear();
        }

        try {
            // no new connection is opened for it: a runtime which does not answer would hold the handshake for the socket timeout,
            // and closing stdin below ends the runtime the same way
            if (!connections.isEmpty() && !myProcessHandler.isProcessTerminated()) {
                sendShutdown(connections.get(0));
            }
        }
        finally {
            for (MongoConnection connection : connections) {
                connection.close();
            }

            // the exit signal of the runtime, which works even when shutdown could not be sent
            closeQuietly(myProcessInput);

            if (!myProcessHandler.waitFor(SHUTDOWN_WAIT_MS)) {
                myProcessHandler.destroyProcess();
            }
        }
    }

    private static void sendShutdown(MongoConnection idleConnection) {
        try {
            idleConnection.getClient().shutdown();
        }
        catch (RuntimeException | TException e) {
            LOG.debug("Cannot send shutdown to the MongoDB runtime, closing its input instead", e);
        }
    }

    private MongoConnection borrowConnection() {
        while (true) {
            MongoConnection connection;
            synchronized (myIdleConnections) {
                connection = myIdleConnections.pollLast();
            }

            if (connection == null) {
                return openConnection();
            }
            if (connection.isUsable()) {
                return connection;
            }
            connection.close();
        }
    }

    private void releaseConnection(MongoConnection connection) {
        boolean pooled;
        synchronized (myIdleConnections) {
            // checked under the lock doClose() drains the pool with, so a connection released while closing is never left open
            pooled = !myClosing && myIdleConnections.size() < MAX_IDLE_CONNECTIONS;
            if (pooled) {
                myIdleConnections.addLast(connection);
            }
        }

        if (!pooled) {
            connection.close();
        }
    }

    /**
     * Opens a connection and runs the handshake: {@code hello} with the client token, then the server token and the protocol version
     * of the answer are checked before the connection is used for anything else.
     */
    private MongoConnection openConnection() {
        TSocket socket = null;
        try {
            TConfiguration configuration = new TConfiguration(MAX_MESSAGE_SIZE, TConfiguration.DEFAULT_MAX_FRAME_SIZE, RECURSION_LIMIT);
            socket = new TSocket(configuration, MongoExecutorConstants.LOOPBACK_HOST, myPort, SOCKET_TIMEOUT_MS, CONNECT_TIMEOUT_MS);
            socket.open();

            MongoExecutor.Client client = new MongoExecutor.Client(new TBinaryProtocol(socket));
            MongoHelloResult hello = client.hello(myClientToken);

            if (!isServerToken(hello.getServerToken())) {
                throw new MongoRuntimeException(MongoLocalize.errorRuntimeHandshake());
            }
            if (hello.getProtocolVersion() != MongoExecutorConstants.PROTOCOL_VERSION) {
                throw new MongoRuntimeException(MongoLocalize.errorRuntimeVersion(hello.getProtocolVersion(),
                    MongoExecutorConstants.PROTOCOL_VERSION));
            }

            MongoConnection connection = new MongoConnection(socket, client);
            socket = null;
            return connection;
        }
        catch (MongoFailError e) {
            throw MongoRuntimeException.of(e);
        }
        catch (TException e) {
            throw MongoRuntimeException.lost(e);
        }
        finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    private boolean isServerToken(@Nullable String token) {
        byte[] expected = myServerToken.getBytes(StandardCharsets.UTF_8);
        byte[] actual = token == null ? new byte[0] : token.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }

    private static String newToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static void closeQuietly(@Nullable OutputStream stream) {
        if (stream == null) {
            return;
        }

        try {
            stream.close();
        }
        catch (IOException ignored) {
        }
    }

    /**
     * A socket to the runtime which passed the handshake. One call at a time uses it.
     */
    private static final class MongoConnection {
        private final TSocket mySocket;
        private final MongoExecutor.Client myClient;

        private long myCallId;
        private boolean myInCall;
        private boolean myAborted;

        private MongoConnection(TSocket socket, MongoExecutor.Client client) {
            mySocket = socket;
            myClient = client;
        }

        MongoExecutor.Client getClient() {
            return myClient;
        }

        synchronized long beginCall() {
            myInCall = true;
            return ++myCallId;
        }

        /**
         * Called by the cancel watcher: closes the socket only when the call is still running, which fails its blocked read.
         */
        synchronized void abort(long callId) {
            if (!myInCall || myCallId != callId || myAborted) {
                return;
            }

            myAborted = true;
            Socket socket = mySocket.getSocket();
            if (socket != null) {
                try {
                    socket.close();
                }
                catch (IOException ignored) {
                }
            }
        }

        /**
         * @return whether the connection is intact, that is the call was not aborted
         */
        synchronized boolean endCall() {
            myInCall = false;
            return !myAborted;
        }

        synchronized boolean isUsable() {
            return !myAborted && mySocket.isOpen();
        }

        void close() {
            mySocket.close();
        }
    }

    /**
     * Reads the ready line from stdout. A chunk may end within a line, so only complete lines are matched, and only the first ready
     * line counts. The runtime prints nothing else to stdout and nothing secret to stderr.
     */
    private static final class RuntimeOutputListener implements ProcessListener {
        private final CompletableFuture<Integer> myPort = new CompletableFuture<>();

        private final StringBuilder myStdout = new StringBuilder();
        private final StringBuilder myStderr = new StringBuilder();

        CompletableFuture<Integer> getPort() {
            return myPort;
        }

        @Override
        public void onTextAvailable(ProcessEvent event, Key outputType) {
            String text = event.getText();
            if (text == null) {
                return;
            }

            if (ProcessOutputType.isStdout(outputType)) {
                onStdout(text);
            }
            else if (ProcessOutputType.isStderr(outputType)) {
                onStderr(text);
            }
        }

        private synchronized void onStdout(String text) {
            myStdout.append(text);

            int lineEnd;
            while ((lineEnd = myStdout.indexOf("\n")) >= 0) {
                String line = myStdout.substring(0, lineEnd).strip();
                myStdout.delete(0, lineEnd + 1);
                onStdoutLine(line);
            }

            if (myStdout.length() > MAX_BUFFERED_OUTPUT) {
                myStdout.setLength(0);
            }
        }

        private void onStdoutLine(String line) {
            if (!myPort.isDone() && line.startsWith(MongoExecutorConstants.READY_LINE_PREFIX)) {
                int port = parsePort(line.substring(MongoExecutorConstants.READY_LINE_PREFIX.length()));
                if (port > 0) {
                    myPort.complete(port);
                    return;
                }
            }

            if (!line.isEmpty()) {
                LOG.debug("MongoDB runtime stdout: " + line);
            }
        }

        private synchronized void onStderr(String text) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("MongoDB runtime stderr: " + text.strip());
            }

            // kept for the log when the runtime fails to start, for example a broken Java installation
            if (myStderr.length() < MAX_BUFFERED_OUTPUT) {
                myStderr.append(text);
            }
        }

        /**
         * @return the port, or {@code 0} when the text is no port number
         */
        private static int parsePort(String text) {
            if (text.isEmpty() || text.length() > 5) {
                return 0;
            }

            int port = 0;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c < '0' || c > '9') {
                    return 0;
                }
                port = port * 10 + (c - '0');
            }
            return port <= 65535 ? port : 0;
        }

        @Override
        public void processTerminated(ProcessEvent event) {
            if (!myPort.isDone()) {
                String stderr;
                synchronized (this) {
                    stderr = myStderr.toString().strip();
                }
                LOG.info("MongoDB runtime exited with code " + event.getExitCode() + " before it was ready" +
                    (stderr.isEmpty() ? "" : ":\n" + stderr));
            }

            myPort.completeExceptionally(new MongoRuntimeException(MongoLocalize.errorRuntimeExited(event.getExitCode())));
        }
    }
}
