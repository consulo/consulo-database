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

package consulo.database.datasource.jdbc.grid;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.database.datasource.model.DataSource;
import consulo.database.jdbc.rt.shared.FailError;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.grid.GridDataHookUpBase;
import consulo.ui.ex.grid.SimpleErrorInfo;
import consulo.ui.grid.DataGridListModel;
import consulo.ui.grid.GridColumn;
import consulo.ui.grid.GridModelUpdater;
import consulo.ui.grid.GridMutationModel;
import consulo.ui.grid.GridRequestSource;
import consulo.ui.grid.GridRow;
import consulo.ui.grid.GridStorageAndModelUpdater;
import consulo.util.lang.ControlFlowException;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * The data source of a grid which shows the rows of a JDBC data source. It holds the models, and runs the requests of the grid
 * in background tasks: a request answers on the UI thread of the grid, through the {@link UIAccess} given by whoever creates the
 * grid there.
 * <p>
 * Read-only: there is no mutator, and the grid gets no cell editors.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@NullMarked
public abstract class JdbcGridDataHookUp extends GridDataHookUpBase<GridRow, GridColumn> implements Disposable {
    private static final Logger LOG = Logger.getInstance(JdbcGridDataHookUp.class);

    /**
     * The work of a request, done on a background thread.
     */
    @FunctionalInterface
    protected interface RequestWork<T> {
        T run(ProgressIndicator indicator) throws Exception;
    }

    /**
     * A failure the user is told about as it is, without a log record.
     */
    protected static final class RequestFailedException extends Exception {
        public RequestFailedException(String message) {
            super(message);
        }
    }

    protected final Project myProject;
    protected final DataSource myDataSource;

    private final UIAccess myUIAccess;
    private final DataGridListModel myDataModel;
    private final GridMutationModel myMutationModel;
    private final GridModelUpdater myModelUpdater;
    private final Set<ProgressIndicator> myRunningIndicators = ConcurrentHashMap.newKeySet();

    private volatile boolean myDisposed;

    /**
     * @param uiAccess the UI thread of the grid, where the requests answer
     */
    protected JdbcGridDataHookUp(Project project, DataSource dataSource, UIAccess uiAccess) {
        myProject = project;
        myDataSource = dataSource;
        myUIAccess = uiAccess;
        // a byte[] or an array value is equal to another one by its content
        myDataModel = new DataGridListModel(Objects::deepEquals);
        // reads the data model through the hook-up, so it is created after it
        myMutationModel = new GridMutationModel(this);
        // read-only, so there are no pending changes to keep
        myModelUpdater = new GridStorageAndModelUpdater(myDataModel, myMutationModel, null);
    }

    @Override
    public DataGridListModel getDataModel() {
        return myDataModel;
    }

    @Override
    public GridMutationModel getMutationModel() {
        return myMutationModel;
    }

    protected GridModelUpdater getModelUpdater() {
        return myModelUpdater;
    }

    /**
     * Replaces the columns of the model, unless they are the same - so a reload keeps the layout of the grid.
     */
    @RequiredUIAccess
    protected void setColumnsIfChanged(List<GridColumn> columns) {
        if (!columns.equals(myDataModel.getColumns())) {
            myModelUpdater.setColumns(columns);
        }
    }

    /**
     * Runs a request of the grid in a cancellable background task. The answer comes on the UI thread of the grid, unless the data
     * source is disposed by then: {@code onLoaded} with the result of the work, which ends the request; otherwise the request
     * finishes without success, and with an error unless it was cancelled.
     */
    @RequiredUIAccess
    protected <T> void runRequest(GridRequestSource source,
                                  LocalizeValue title,
                                  RequestWork<T> work,
                                  BiConsumer<GridRequestSource, T> onLoaded) {
        notifyRequestStarted(source);
        if (myDisposed) {
            notifyRequestFinished(source, false);
            return;
        }

        // the task may report a cancellation twice - in run() and in onCancel()
        AtomicBoolean answered = new AtomicBoolean();
        new Task.Backgroundable(myProject, title, true) {
            @Override
            public void run(ProgressIndicator indicator) {
                myRunningIndicators.add(indicator);
                try {
                    if (myDisposed) {
                        indicator.cancel();
                    }
                    indicator.checkCanceled();

                    T result = work.run(indicator);

                    answer(answered, () -> onLoaded.accept(source, result));
                }
                catch (Exception e) {
                    if (e instanceof ControlFlowException || indicator.isCanceled()) {
                        answer(answered, () -> notifyRequestFinished(source, false));
                    }
                    else {
                        answer(answered, () -> fail(source, e));
                    }
                }
                finally {
                    myRunningIndicators.remove(indicator);
                }
            }

            @RequiredUIAccess
            @Override
            public void onCancel() {
                answer(answered, () -> notifyRequestFinished(source, false));
            }

            @RequiredUIAccess
            @Override
            public void onThrowable(Throwable error) {
                answer(answered, () -> fail(source, error));

                super.onThrowable(error);
            }
        }.queue();
    }

    /**
     * Ends a request which needs no work: it changes nothing, and succeeds.
     */
    @RequiredUIAccess
    protected void finishUnchanged(GridRequestSource source) {
        notifyRequestStarted(source);
        notifyRequestFinished(source, true);
    }

    private void answer(AtomicBoolean answered, @RequiredUIAccess Runnable answer) {
        if (!answered.compareAndSet(false, true)) {
            return;
        }

        myUIAccess.give(() -> {
            // a disposed grid listens no more
            if (!myDisposed) {
                answer.run();
            }
        });
    }

    @RequiredUIAccess
    private void fail(GridRequestSource source, Throwable error) {
        if (!(error instanceof FailError) && !(error instanceof RequestFailedException)) {
            LOG.warn(error);
        }

        notifyRequestError(source, SimpleErrorInfo.create(getErrorMessage(error), error));
        notifyRequestFinished(source, false);
    }

    /**
     * @return the message of the database, after its SQL state when it gives one
     */
    private static String getErrorMessage(Throwable error) {
        String message = error.getMessage();
        if (StringUtil.isEmpty(message)) {
            message = error.getClass().getSimpleName();
        }

        if (error instanceof FailError failError && failError.isSetSqlState() && !StringUtil.isEmpty(failError.getSqlState())) {
            return "[" + failError.getSqlState() + "] " + message;
        }
        return message;
    }

    protected boolean isDisposed() {
        return myDisposed;
    }

    @Override
    public void dispose() {
        myDisposed = true;

        // stops the transport session (the runtime process) of a request which is still running
        for (ProgressIndicator indicator : myRunningIndicators) {
            indicator.cancel();
        }
    }
}
