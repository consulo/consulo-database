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

import com.mongodb.client.MongoClient;
import consulo.database.mongo.rt.shared.MongoConnectSettings;
import consulo.database.mongo.rt.shared.MongoErrorKind;
import consulo.database.mongo.rt.shared.MongoFailError;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The state of the process, shared by all connections: the tokens of the handshake and the one {@link MongoClient}, which is
 * thread safe and keeps its pooled connections open between calls.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
final class MongoRuntimeState {
    /**
     * How long the exit waits for the client to close - closing ends the server sessions over the network.
     */
    private static final long CLOSE_WAIT_MS = 2000;

    /**
     * How many queries may encode their answers at the same time. An answer is a tree of objects many times larger than its BSON
     * size, so the slots bound the memory of the process.
     */
    private static final int QUERY_SLOTS = 2;

    private final byte[] myClientToken;
    private final String myServerToken;

    private final Object myLock = new Object();
    private @Nullable MongoClient myClient;
    private String myAuthSource = MongoClientSettingsFactory.DEFAULT_AUTH_SOURCE;
    private boolean myClosed;

    private final Semaphore myQuerySlots = new Semaphore(QUERY_SLOTS, true);
    private final AtomicBoolean myExiting = new AtomicBoolean();

    MongoRuntimeState(String clientToken, String serverToken) {
        myClientToken = clientToken.getBytes(StandardCharsets.UTF_8);
        myServerToken = serverToken;
    }

    /**
     * Compares in constant time, so the answer time tells nothing about the token.
     */
    boolean isValidClientToken(@Nullable String token) {
        return token != null && MessageDigest.isEqual(myClientToken, token.getBytes(StandardCharsets.UTF_8));
    }

    String getServerToken() {
        return myServerToken;
    }

    /**
     * Creates the client of the process, replacing the previous one. Creating a client does not wait for a server: background
     * threads of the driver connect.
     */
    void connect(MongoConnectSettings settings) throws MongoFailError {
        // created outside of the lock: parsing a mongodb+srv string looks up a DNS record
        MongoClient client = MongoClientSettingsFactory.createClient(settings, false);
        String authSource = MongoClientSettingsFactory.getAuthSource(settings);

        @Nullable MongoClient clientToClose;
        synchronized (myLock) {
            if (myClosed) {
                clientToClose = client;
            }
            else {
                clientToClose = myClient;
                myClient = client;
                myAuthSource = authSource;
            }
        }

        if (clientToClose != null) {
            close(clientToClose);
        }
    }

    MongoClient getClient() throws MongoFailError {
        synchronized (myLock) {
            @Nullable MongoClient client = myClient;
            if (client == null) {
                throw MongoErrors.fail(MongoErrorKind.NOT_CONNECTED, myClosed ? "The runtime is exiting" : "connect() was not called");
            }
            return client;
        }
    }

    String getAuthSource() {
        synchronized (myLock) {
            return myAuthSource;
        }
    }

    /**
     * Closes the client and exits the process, once. Returns at once: the exit runs on a thread of its own, so a oneway
     * shutdown() and the stdin watcher are not blocked.
     */
    void exit() {
        if (!myExiting.compareAndSet(false, true)) {
            return;
        }

        Thread exitThread = new Thread(() -> {
            @Nullable MongoClient client;
            synchronized (myLock) {
                myClosed = true;
                client = myClient;
                myClient = null;
            }

            if (client != null) {
                MongoClient clientToClose = client;
                Thread closeThread = new Thread(() -> close(clientToClose), "Mongo Runtime Close");
                closeThread.setDaemon(true);
                closeThread.start();
                try {
                    closeThread.join(CLOSE_WAIT_MS);
                }
                catch (InterruptedException ignored) {
                    // exit anyway
                }
            }

            System.exit(0);
        }, "Mongo Runtime Exit");
        exitThread.start();
    }

    /**
     * Waits for a query slot as long as the query itself may run.
     *
     * @param maxTimeMs the time limit of the query, 0 or less for none
     */
    void acquireQuerySlot(int maxTimeMs) throws MongoFailError, InterruptedException {
        long waitMs = maxTimeMs > 0 ? maxTimeMs : TimeUnit.MINUTES.toMillis(1);
        if (!myQuerySlots.tryAcquire(waitMs, TimeUnit.MILLISECONDS)) {
            throw MongoErrors.fail(MongoErrorKind.TIMEOUT, "The MongoDB runtime is busy with other queries");
        }
    }

    void releaseQuerySlot() {
        myQuerySlots.release();
    }

    private static void close(MongoClient client) {
        try {
            client.close();
        }
        catch (RuntimeException ignored) {
            // nothing to report to: the client is dropped anyway
        }
    }
}
