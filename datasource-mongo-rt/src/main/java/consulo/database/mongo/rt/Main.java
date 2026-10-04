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

package consulo.database.mongo.rt;

import consulo.database.mongo.rt.shared.MongoExecutor;
import consulo.database.mongo.rt.shared.MongoExecutorConstants;
import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.ServerContext;
import org.apache.thrift.server.TServerEventHandler;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * The entry point of the MongoDB runtime process, started by the IDE with the driver jars on the class path.
 * <ol>
 * <li>The IDE writes two lines to stdin: the client token, which {@code hello()} must send, and the server token, which
 * {@code hello()} answers with. Neither is on the command line.</li>
 * <li>The runtime listens on an ephemeral port of {@link MongoExecutorConstants#LOOPBACK_HOST} and prints
 * {@link MongoExecutorConstants#READY_LINE_PREFIX} and the port to stdout. Nothing else goes to stdout.</li>
 * <li>When stdin is closed - the IDE closed it, or the IDE died - the runtime exits.</li>
 * </ol>
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public final class Main {
    /**
     * A connection must call hello() within this time, or it is closed: connections which never authenticate would otherwise
     * hold the worker threads.
     */
    private static final int HELLO_TIMEOUT_MS = 10_000;

    private static final int MAX_WORKER_THREADS = 32;

    private static final int EXIT_NO_TOKENS = 2;

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        // stdout carries the ready line only, anything a library prints goes to stderr
        PrintStream stdout = System.out;
        System.setOut(System.err);

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        @Nullable String clientToken = readToken(stdin);
        @Nullable String serverToken = readToken(stdin);
        if (clientToken == null || serverToken == null) {
            System.err.println("The MongoDB runtime expects two tokens on stdin");
            System.exit(EXIT_NO_TOKENS);
            return;
        }

        MongoRuntimeState state = new MongoRuntimeState(clientToken, serverToken);
        startParentWatcher(stdin, state);

        InetSocketAddress bindAddress = new InetSocketAddress(InetAddress.getByName(MongoExecutorConstants.LOOPBACK_HOST), 0);
        TServerSocket serverSocket = new TServerSocket(bindAddress, HELLO_TIMEOUT_MS);
        int port = serverSocket.getServerSocket().getLocalPort();

        TThreadPoolServer.Args serverArgs = new TThreadPoolServer.Args(serverSocket)
            .minWorkerThreads(1)
            .maxWorkerThreads(MAX_WORKER_THREADS);
        // a handler per connection: it keeps whether its connection passed hello()
        serverArgs.processorFactory(new TProcessorFactory(null) {
            @Override
            public TProcessor getProcessor(TTransport transport) {
                @Nullable TSocket socket = transport instanceof TSocket tSocket ? tSocket : null;
                return new MongoExecutor.Processor<>(new MongoExecutorImpl(state, socket));
            }
        });

        TThreadPoolServer server = new TThreadPoolServer(serverArgs);
        server.setServerEventHandler(new TServerEventHandler() {
            @Override
            public void preServe() {
                // one write: the IDE must never see a part of the line
                byte[] line = (MongoExecutorConstants.READY_LINE_PREFIX + port + "\n").getBytes(StandardCharsets.UTF_8);
                stdout.write(line, 0, line.length);
                stdout.flush();
            }

            @Override
            public @Nullable ServerContext createContext(TProtocol input, TProtocol output) {
                return null;
            }

            @Override
            public void deleteContext(@Nullable ServerContext serverContext, TProtocol input, TProtocol output) {
            }

            @Override
            public void processContext(@Nullable ServerContext serverContext, TTransport inputTransport, TTransport outputTransport) {
            }
        });

        server.serve();
    }

    private static @Nullable String readToken(BufferedReader stdin) throws IOException {
        @Nullable String line = stdin.readLine();
        if (line == null) {
            return null;
        }

        String token = line.strip();
        return token.isEmpty() ? null : token;
    }

    /**
     * The IDE keeps stdin open for the life of the process: end of input means it closed the session or died.
     */
    private static void startParentWatcher(BufferedReader stdin, MongoRuntimeState state) {
        Thread watcher = new Thread(() -> {
            try {
                while (stdin.read() >= 0) {
                    // later input has no meaning, it is skipped
                }
            }
            catch (IOException ignored) {
                // a broken pipe ends the input as well
            }
            state.exit();
        }, "Mongo Runtime Parent Watcher");
        watcher.setDaemon(true);
        watcher.start();
    }
}
