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

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.concurrent.ApplicationConcurrency;
import consulo.application.progress.ProgressIndicator;
import consulo.component.ProcessCanceledException;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.DataSourceEvent;
import consulo.database.datasource.model.DataSourceListener;
import consulo.database.mongo.rt.shared.MongoConnectSettings;
import consulo.database.mongo.transport.MongoConnectionSettings;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.process.ExecutionException;
import consulo.project.Project;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Keeps one MongoDB runtime process per data source of the project, connected with the settings of the data source: paging,
 * counting and re-filtering a collection must not pay a JVM start, a TLS handshake and an authentication each time, as a runtime per
 * call would. Test Connection does not use it - it starts a one-shot runtime with the unapplied settings of the dialog.
 * <p>
 * A cached runtime is keyed by the data source id and the {@link MongoConnectionSettings#getFingerprint() fingerprint} of the
 * settings it was connected with: the settings dialog edits a copy carrying the same id, and changed settings must never reuse a
 * runtime of the old ones. A runtime ends when its data source is removed, when it was idle for
 * {@value #DEFAULT_IDLE_TIMEOUT_MINUTES} minutes, when its settings changed and it is asked for again, and when the project is
 * closed - always after its running calls are done.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@Singleton
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class MongoSessionService implements Disposable {
    private static final Logger LOG = Logger.getInstance(MongoSessionService.class);

    private static final long DEFAULT_IDLE_TIMEOUT_MINUTES = 10;

    private static final long IDLE_TIMEOUT_MINUTES = Long.getLong("consulo.database.mongo.idle.minutes", DEFAULT_IDLE_TIMEOUT_MINUTES);

    private static final long POLL_MS = 100;

    private static final class Entry {
        private final String myFingerprint;
        private final CompletableFuture<MongoSession> myFuture = new CompletableFuture<>();

        private Entry(String fingerprint) {
            myFingerprint = fingerprint;
        }

        /**
         * @return the session once started, {@code null} while starting or when the start failed
         */
        @Nullable MongoSession getStartedSession() {
            if (!myFuture.isDone() || myFuture.isCompletedExceptionally()) {
                return null;
            }
            return myFuture.join();
        }
    }

    private final ScheduledExecutorService myScheduler;
    private final Executor myCloseExecutor;

    private final Map<UUID, Entry> myEntries = new HashMap<>();

    private final ScheduledFuture<?> myIdleChecker;

    private boolean myDisposed;

    public static MongoSessionService getInstance(Project project) {
        return project.getInstance(MongoSessionService.class);
    }

    @Inject
    public MongoSessionService(ApplicationConcurrency concurrency, Project project) {
        myScheduler = concurrency.getScheduledExecutorService();
        // closing waits for the process to exit, which must not block the UI thread a data source event comes on
        myCloseExecutor = concurrency.executor();

        project.getMessageBus().connect(this).subscribe(DataSourceListener.class, this::onDataSourceEvent);

        myIdleChecker = myScheduler.scheduleWithFixedDelay(this::closeIdleSessions, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * Reads the settings of a data source and checks them before anything is started. Must be called off the UI thread: reading the
     * password may ask the password safe.
     *
     * @throws MongoRuntimeException the connection string is invalid - it must never reach a runtime when it holds a password
     */
    public static MongoConnectionSettings readSettings(DataSource dataSource) {
        MongoConnectionSettings settings = MongoConnectionSettings.of(dataSource);
        LocalizeValue error = MongoConnectionSettings.checkConnectionString(settings.getConnectionString());
        if (error != null) {
            throw new MongoRuntimeException(error);
        }
        return settings;
    }

    /**
     * Must be called off the UI thread: it may read the password, download the driver and start a runtime.
     *
     * @return the runtime of the data source, connected with its current settings
     */
    public MongoSession getSession(ProgressIndicator indicator, DataSource dataSource) throws IOException, ExecutionException {
        return getSession(indicator, dataSource, true);
    }

    private MongoSession getSession(ProgressIndicator indicator,
                                    DataSource dataSource,
                                    boolean retryCanceledStart) throws IOException, ExecutionException {
        MongoConnectionSettings settings = readSettings(dataSource);
        String fingerprint = settings.getFingerprint();
        UUID id = dataSource.getId();

        Entry entry;
        @Nullable Entry staleEntry = null;
        boolean starter = false;
        synchronized (myEntries) {
            if (myDisposed) {
                throw new ProcessCanceledException();
            }

            entry = myEntries.get(id);
            if (entry != null && !isReusable(entry, fingerprint)) {
                myEntries.remove(id);
                staleEntry = entry;
                entry = null;
            }

            if (entry == null) {
                entry = new Entry(fingerprint);
                myEntries.put(id, entry);
                starter = true;
            }
            else {
                MongoSession session = entry.getStartedSession();
                if (session != null) {
                    // under the lock, so the idle check cannot close it in between
                    session.touch();
                    return session;
                }
            }
        }

        if (staleEntry != null) {
            // other settings, or the process died - closed once its running calls are done
            retire(staleEntry);
        }

        if (starter) {
            return startSession(indicator, dataSource, settings, id, entry);
        }
        // another thread is starting the runtime of this data source
        return awaitSession(indicator, dataSource, entry, retryCanceledStart);
    }

    private static boolean isReusable(Entry entry, String fingerprint) {
        if (!entry.myFingerprint.equals(fingerprint) || entry.myFuture.isCompletedExceptionally()) {
            return false;
        }

        MongoSession session = entry.getStartedSession();
        return session == null || session.isAlive();
    }

    private MongoSession startSession(ProgressIndicator indicator,
                                      DataSource dataSource,
                                      MongoConnectionSettings settings,
                                      UUID id,
                                      Entry entry) throws IOException, ExecutionException {
        MongoSession session = null;
        try {
            session = MongoSession.start(indicator, dataSource, myScheduler);

            MongoConnectSettings runtimeSettings = settings.toRuntimeSettings(false);
            session.<@Nullable Void>execute(indicator, client -> {
                client.connect(runtimeSettings);
                return null;
            });
        }
        catch (Throwable e) {
            // a started runtime would live until the IDE exits - nothing else can reach it once the entry is gone
            if (session != null) {
                session.closeWhenIdle(myCloseExecutor);
            }

            synchronized (myEntries) {
                myEntries.remove(id, entry);
            }
            entry.myFuture.completeExceptionally(e);
            throw e;
        }

        // a data source removed meanwhile, or the project closed: retire() of the entry closes the session on completion
        entry.myFuture.complete(session);
        return session;
    }

    private MongoSession awaitSession(ProgressIndicator indicator,
                                      DataSource dataSource,
                                      Entry entry,
                                      boolean retryCanceledStart) throws IOException, ExecutionException {
        while (true) {
            try {
                return entry.myFuture.get(POLL_MS, TimeUnit.MILLISECONDS);
            }
            catch (TimeoutException e) {
                indicator.checkCanceled();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ProcessCanceledException();
            }
            catch (java.util.concurrent.ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof ProcessCanceledException && retryCanceledStart && !indicator.isCanceled()) {
                    // the starter was cancelled, this caller was not - start again
                    return getSession(indicator, dataSource, false);
                }
                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                if (cause instanceof Error error) {
                    throw error;
                }
                if (cause instanceof IOException ioException) {
                    throw new IOException(ioException.getMessage(), ioException);
                }
                if (cause instanceof ExecutionException executionException) {
                    throw new ExecutionException(executionException.getMessage(), executionException);
                }
                throw new IllegalStateException(cause);
            }
        }
    }

    /**
     * A changed data source needs nothing here: the settings dialog fires a change for every data source on OK, and a runtime whose
     * settings did change is replaced by the fingerprint check of the next {@link #getSession}.
     */
    private void onDataSourceEvent(DataSourceEvent event) {
        if (event.getAction() != DataSourceEvent.Action.REMOVE) {
            return;
        }

        Entry entry;
        synchronized (myEntries) {
            entry = myEntries.remove(event.getDataSource().getId());
        }

        if (entry != null) {
            retire(entry);
        }
    }

    private void closeIdleSessions() {
        try {
            long idleNanos = TimeUnit.MINUTES.toNanos(IDLE_TIMEOUT_MINUTES);

            List<MongoSession> sessions = new ArrayList<>();
            synchronized (myEntries) {
                Iterator<Entry> iterator = myEntries.values().iterator();
                while (iterator.hasNext()) {
                    MongoSession session = iterator.next().getStartedSession();
                    if (session != null && (!session.isAlive() || session.isIdleFor(idleNanos))) {
                        iterator.remove();
                        sessions.add(session);
                    }
                }
            }

            for (MongoSession session : sessions) {
                session.closeWhenIdle(myCloseExecutor);
            }
        }
        catch (Throwable e) {
            // an exception would cancel the periodic check
            LOG.warn("Failed to close idle MongoDB runtimes", e);
        }
    }

    @Override
    public void dispose() {
        myIdleChecker.cancel(false);

        List<Entry> entries;
        synchronized (myEntries) {
            myDisposed = true;
            entries = new ArrayList<>(myEntries.values());
            myEntries.clear();
        }

        // if the IDE exits before a pooled close runs, the runtime still exits: its stdin closes with the IDE
        for (Entry entry : entries) {
            retire(entry);
        }
    }

    /**
     * Closes the session of an entry once its calls are done - for a session still starting, once it started.
     */
    private void retire(Entry entry) {
        entry.myFuture.thenAccept(session -> session.closeWhenIdle(myCloseExecutor));
    }
}
