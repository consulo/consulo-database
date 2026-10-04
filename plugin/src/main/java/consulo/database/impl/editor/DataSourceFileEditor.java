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

package consulo.database.impl.editor;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.component.ProcessCanceledException;
import consulo.dataContext.UiDataProvider;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransport;
import consulo.database.datasource.transport.DataSourceTransportResult;
import consulo.database.datasource.transport.ui.DataSourceTransportResultPresentation;
import consulo.database.impl.editor.actions.RefreshDataAction;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.fileEditor.FileEditor;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.ActionToolbar;
import consulo.ui.layout.DockLayout;
import consulo.ui.layout.LoadingLayout;
import consulo.ui.style.StandardColors;
import consulo.util.concurrent.AsyncResult;
import consulo.util.dataholder.UserDataHolderBase;
import consulo.util.lang.ControlFlowException;
import consulo.util.lang.StringUtil;
import kava.beans.PropertyChangeListener;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The rows of a child of a database - a table, a collection. A presentation which has a view loading its rows by itself (the data
 * grid, with its own paging and Reload) fills the editor with it. Otherwise the editor fetches the rows through the transport, shows
 * them with the presentation, and has a toolbar to fetch them again.
 *
 * @author VISTALL
 * @since 2020-08-19
 */
public class DataSourceFileEditor extends UserDataHolderBase implements FileEditor {
    private static final Logger LOG = Logger.getInstance(DataSourceFileEditor.class);

    private final Project myProject;
    private final DataSourceVirtualFile myFile;
    private final DataSource myDataSource;

    private final DockLayout myRootLayout;
    /**
     * Disposes the view of the rows - the grid and its data source, or the fetched result
     */
    private final Disposable myViewDisposable;
    /**
     * Holds the fetched result, null when the view loads its rows by itself
     */
    private final @Nullable LoadingLayout<DockLayout> myLoadingLayout;

    private final AtomicBoolean myLoading = new AtomicBoolean();
    private volatile @Nullable ProgressIndicator myLoadingIndicator;
    private volatile boolean myDisposed;

    private @Nullable Disposable myLastResultDisposable;

    @RequiredUIAccess
    public DataSourceFileEditor(Project project, DataSourceVirtualFile file) {
        myProject = project;
        myFile = file;
        myDataSource = myFile.getDataSource();

        myViewDisposable = Disposable.newDisposable("DataSourceFileEditor view");
        Disposer.register(this, myViewDisposable);

        myRootLayout = DockLayout.create();
        myRootLayout.putUserData(UiDataProvider.KEY, sink -> sink.set(DataSourceFileEditorKeys.EDITOR, this));

        DataSourceTransportResultPresentation<?> presentation = findPresentation(project, myDataSource);
        Component childView = presentation == null
            ? null
            : presentation.buildComponentForChild(project, myDataSource, myFile.getDatabaseName(), myFile.getChildId(), myViewDisposable);

        if (childView != null) {
            // it loads the first page once it is shown, and has its own paging and Reload
            myRootLayout.center(childView);
            myLoadingLayout = null;
        }
        else {
            myLoadingLayout = createFetchedResultLayout();
            loadData();
        }
    }

    @RequiredUIAccess
    private LoadingLayout<DockLayout> createFetchedResultLayout() {
        ActionGroup.Builder builder = ActionGroup.newImmutableBuilder();
        builder.add(new RefreshDataAction());

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DataSourceEditor", builder.build(), true);
        toolbar.setTargetUIComponent(myRootLayout);

        // the toolbar component itself may be a bridge which ignores borders - so the border goes to a wrapper
        DockLayout toolbarLayout = DockLayout.create();
        toolbarLayout.center(toolbar.getUIComponent());
        toolbarLayout.borderBuilder().bottomSet().apply();
        myRootLayout.top(toolbarLayout);

        // only the result area is covered while loading, the toolbar stays reachable
        LoadingLayout<DockLayout> loadingLayout = LoadingLayout.create(DockLayout.create(), myViewDisposable);
        loadingLayout.setLoadingText(LocalizeValue.localizeTODO("Fetching data..."));
        myRootLayout.center(loadingLayout);
        return loadingLayout;
    }

    public boolean isLoading() {
        return myLoading.get();
    }

    /**
     * Fetches the rows again, unless the view loads them by itself
     */
    @RequiredUIAccess
    public void loadData() {
        LoadingLayout<DockLayout> loadingLayout = myLoadingLayout;
        if (loadingLayout == null || myDisposed || !myLoading.compareAndSet(false, true)) {
            return;
        }

        UIAccess uiAccess = UIAccess.current();

        disposeLastResult();
        loadingLayout.startLoading();

        Project project = myProject;
        DataSource dataSource = myDataSource;
        String databaseName = myFile.getDatabaseName();
        String childId = myFile.getChildId();

        AsyncResult<DataSourceTransportResult> result = AsyncResult.undefined();
        // done and rejected both end here - on the ui thread
        result.toCompletableFuture()
            .whenCompleteAsync((transportResult, error) -> onDataLoaded(loadingLayout, transportResult, error), uiAccess);

        new Task.Backgroundable(project, LocalizeValue.localizeTODO("Fetching data..."), true) {
            @Override
            public void run(ProgressIndicator indicator) {
                myLoadingIndicator = indicator;

                if (myDisposed) {
                    result.rejectWithThrowable(new ProcessCanceledException());
                    return;
                }

                DataSourceTransport<?> transport = findTransport(project, dataSource);
                if (transport == null) {
                    result.reject("There is no transport for data source '" + dataSource.getName() + "'");
                    return;
                }

                transport.fetchData(indicator, project, dataSource, databaseName, childId, result);
            }

            @RequiredUIAccess
            @Override
            public void onCancel() {
                rejectIfPending(result, new ProcessCanceledException());
            }

            @RequiredUIAccess
            @Override
            public void onThrowable(Throwable error) {
                rejectIfPending(result, error);

                super.onThrowable(error);
            }
        }.queue();
    }

    private static void rejectIfPending(AsyncResult<?> result, Throwable error) {
        if (!result.isProcessed()) {
            result.rejectWithThrowable(error);
        }
    }

    @RequiredUIAccess
    private void onDataLoaded(LoadingLayout<DockLayout> loadingLayout,
                              @Nullable DataSourceTransportResult transportResult,
                              @Nullable Throwable error) {
        myLoadingIndicator = null;
        myLoading.set(false);

        if (myDisposed) {
            return;
        }

        loadingLayout.stopLoading(layout -> {
            if (error != null) {
                layout.center(createErrorComponent(error));
                return;
            }

            if (transportResult == null) {
                layout.center(Label.create(LocalizeValue.localizeTODO("No data")));
                return;
            }

            Disposable resultDisposable = Disposable.newDisposable("DataSourceFileEditor result");
            Disposer.register(myViewDisposable, resultDisposable);
            myLastResultDisposable = resultDisposable;

            try {
                String databaseName = myFile.getDatabaseName();
                String childId = myFile.getChildId();

                layout.center(buildUI(transportResult, myProject, myDataSource, databaseName, childId, resultDisposable));
            }
            catch (RuntimeException e) {
                LOG.error(e);

                disposeLastResult();

                layout.center(createErrorComponent(e));
            }
        });
    }

    @RequiredUIAccess
    private static Component createErrorComponent(Throwable error) {
        if (error instanceof ControlFlowException) {
            return Label.create(LocalizeValue.localizeTODO("Fetching data was cancelled"));
        }

        String message = error.getMessage();
        if (StringUtil.isEmpty(message)) {
            message = error.getClass().getSimpleName();
        }

        Label label = Label.create(LocalizeValue.join(LocalizeValue.localizeTODO("Failed to fetch data: "), LocalizeValue.of(message)));
        label.setForegroundColor(StandardColors.RED);
        return label;
    }

    private static @Nullable DataSourceTransport<?> findTransport(Project project, DataSource dataSource) {
        return project.getApplication().getExtensionPoint(DataSourceTransport.class).findFirstSafe(it -> it.accept(dataSource));
    }

    public static @Nullable DataSourceTransportResultPresentation<?> findPresentation(Project project, DataSource dataSource) {
        return project.getApplication()
            .getExtensionPoint(DataSourceTransportResultPresentation.class)
            .findFirstSafe(it -> it.accept(dataSource));
    }

    /**
     * @return the parts of a result which are shown in views of their own, see {@link DataSourceTransportResultPresentation#splitResult}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<?> splitResult(@Nullable Object result, Project project, DataSource dataSource) {
        DataSourceTransportResultPresentation presentation = result == null ? null : findPresentation(project, dataSource);
        if (presentation == null) {
            return Collections.singletonList(result);
        }
        return presentation.splitResult(result);
    }

    @RequiredUIAccess
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Component buildUI(@Nullable Object result,
                                    Project project,
                                    DataSource dataSource,
                                    @Nullable String dbName,
                                    @Nullable String childId,
                                    Disposable parent) {
        if (result == null) {
            return Label.create(LocalizeValue.localizeTODO("No data"));
        }

        DataSourceTransportResultPresentation target = findPresentation(project, dataSource);
        if (target == null) {
            return Label.create(LocalizeValue.localizeTODO("Not supported result"));
        }

        return target.buildComponentForResult(result, project, dataSource, dbName, childId, parent);
    }

    private void disposeLastResult() {
        Disposable lastResultDisposable = myLastResultDisposable;
        if (lastResultDisposable != null) {
            myLastResultDisposable = null;
            Disposer.dispose(lastResultDisposable);
        }
    }

    @Override
    public Component getUIComponent() {
        return myRootLayout;
    }

    @Override
    public String getName() {
        return "datasource";
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    }

    @Override
    public void dispose() {
        myDisposed = true;

        // stops the transport session (the rt process) of a fetch which is still running
        ProgressIndicator indicator = myLoadingIndicator;
        if (indicator != null) {
            indicator.cancel();
        }

        // the grid and its data source - which stops a load of the grid which is still running - or the fetched result
        myLastResultDisposable = null;
        Disposer.dispose(myViewDisposable);
    }
}
