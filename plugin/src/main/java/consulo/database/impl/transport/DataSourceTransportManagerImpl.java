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

package consulo.database.impl.transport;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ServiceImpl;
import consulo.application.progress.PerformInBackgroundOption;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.database.datasource.DataSourceManager;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.model.DataSourceEvent;
import consulo.database.datasource.model.DataSourceListener;
import consulo.database.datasource.transport.DataSourceTransport;
import consulo.database.datasource.transport.DataSourceTransportListener;
import consulo.database.datasource.transport.DataSourceTransportManager;
import consulo.database.datasource.transport.FakeDataSourceTransport;
import consulo.database.impl.DatabaseNotificationGroupContributor;
import consulo.database.impl.localize.DatabaseLocalize;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.project.ui.notification.NotificationService;
import consulo.util.concurrent.AsyncResult;
import consulo.util.lang.ControlFlowException;
import consulo.util.lang.StringUtil;
import consulo.util.lang.xml.XmlStringUtil;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jdom.Element;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
@Singleton
@State(name = "DataSourceTransportManagerImpl", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
@ServiceImpl
public class DataSourceTransportManagerImpl implements DataSourceTransportManager, PersistentStateComponent<Element> {
    private static final Logger LOG = Logger.getInstance(DataSourceTransportManagerImpl.class);

    private final Project myProject;

    private final DataSourceManager myDataSourceManager;

    private final NotificationService myNotificationService;

    private final Map<UUID, DataSourceState> myStates = new ConcurrentHashMap<>();

    @Inject
    public DataSourceTransportManagerImpl(Project project, DataSourceManager dataSourceManager, NotificationService notificationService) {
        myProject = project;
        myDataSourceManager = dataSourceManager;
        myNotificationService = notificationService;

        myProject.getMessageBus().connect().subscribe(DataSourceListener.class, new DataSourceListener() {
            @Override
            public void dataSourceEvent(DataSourceEvent event) {
                if (event.getAction() == DataSourceEvent.Action.REMOVE) {
                    myStates.remove(event.getDataSource().getId());
                }
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public AsyncResult<Void> testConnection(DataSource dataSource) {
        DataSourceTransport transport = findTransport(dataSource);

        AsyncResult<Void> result = AsyncResult.undefined();

        // Task has its own myProject field (a ComponentManager)
        Project project = myProject;
        new Task.ConditionalModal(project, DatabaseLocalize.progressTestingConnection(), true, PerformInBackgroundOption.DEAF) {
            @Override
            public void run(ProgressIndicator indicator) {
                transport.testConnection(indicator, project, dataSource, result);

                result.waitFor(-1);
            }
        }.queue();
        return result;
    }

    @RequiredReadAction
    @Override
    @SuppressWarnings("unchecked")
    public void refreshAll() {
        List<? extends DataSource> dataSources = myDataSourceManager.getDataSources();

        DataSourceTransportListener publisher = myProject.getMessageBus().syncPublisher(DataSourceTransportListener.class);

        Task.Backgroundable.queue(myProject, DatabaseLocalize.progressRefreshingDataSources(), true, indicator -> {
            for (DataSource dataSource : dataSources) {
                DataSourceTransport transport = findTransport(dataSource);
                if (transport instanceof FakeDataSourceTransport) {
                    // the fake transport only rejects - say why instead of reporting an UnsupportedOperationException
                    LocalizeValue providerName = dataSource.getProvider().getName();
                    notifyRefreshFailed(dataSource, DatabaseLocalize.notificationRefreshNoTransportText(providerName));
                    continue;
                }

                AsyncResult<PersistentStateComponent<?>> result = AsyncResult.undefined();

                // done, rejected and rejected with a throwable all end here (on the thread which completes the result)
                result.toCompletableFuture().whenComplete((state, error) -> {
                    try {
                        if (error != null) {
                            if (!(error instanceof ControlFlowException) && !indicator.isCanceled()) {
                                notifyRefreshFailed(dataSource, error);
                            }
                        }
                        else if (state != null) {
                            myStates.put(dataSource.getId(), new DataSourceState(transport.getStateVersion(), null, state));

                            publisher.dataUpdated(dataSource, state);
                        }
                    }
                    catch (RuntimeException e) {
                        // whenComplete would swallow it
                        if (!(e instanceof ControlFlowException)) {
                            LOG.error(e);
                        }
                    }
                });

                try {
                    transport.loadInitialData(indicator, myProject, dataSource, result);
                }
                catch (RuntimeException e) {
                    if (e instanceof ControlFlowException) {
                        throw e;
                    }

                    // one broken transport must not stop the refresh of the other data sources
                    LOG.warn(e);

                    if (!result.isProcessed()) {
                        result.rejectWithThrowable(e);
                    }
                }
            }
        });
    }

    private void notifyRefreshFailed(DataSource dataSource, Throwable error) {
        String message = error.getMessage();
        if (StringUtil.isEmpty(message)) {
            message = error.getClass().getSimpleName();
        }

        notifyRefreshFailed(dataSource, LocalizeValue.of(XmlStringUtil.escapeString(message)));
    }

    private void notifyRefreshFailed(DataSource dataSource, LocalizeValue reason) {
        myNotificationService.newError(DatabaseNotificationGroupContributor.DATABASE_GROUP)
            .title(DatabaseLocalize.notificationRefreshFailedTitle())
            .subtitle(LocalizeValue.of(XmlStringUtil.escapeString(dataSource.getName())))
            .content(reason)
            .notify(myProject);
    }

    private DataSourceTransport findTransport(DataSource dataSource) {
        DataSourceTransport dataSourceTransport = null;
        for (DataSourceTransport transport : DataSourceTransport.EP_NAME.getExtensionList()) {
            if (transport.accept(dataSource)) {
                dataSourceTransport = transport;
                break;
            }
        }

        if (dataSourceTransport == null) {
            throw new UnsupportedOperationException("No fake transport. Broken distribution");
        }

        return dataSourceTransport;
    }

    @Override
    public <T extends PersistentStateComponent<?>> @Nullable T getDataState(DataSource dataSource) {
        DataSourceState dataSourceState = myStates.get(dataSource.getId());
        if (dataSourceState == null) {
            return null;
        }

        DataSourceTransport transport = findTransport(dataSource);

        if (dataSourceState.getVersion() != transport.getStateVersion()) {
            myStates.remove(dataSource.getId());
            return null;
        }

        return dataSourceState.getObjectState(dataSource, transport);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AsyncResult<Object> runQuery(DataSource dataSource, String query) {
        DataSourceTransport transport = findTransport(dataSource);

        AsyncResult<Object> result = AsyncResult.undefined();

        Task.Backgroundable.queue(myProject, DatabaseLocalize.progressExecutingQuery(), true, indicator -> {
            transport.runQuery(indicator, myProject, dataSource, query, result);
        });

        return result;
    }

    @Override
    public @Nullable Element getState() {
        Element rootElement = new Element("state");
        for (Map.Entry<UUID, DataSourceState> entry : myStates.entrySet()) {
            Element stateElement = new Element("datasource");

            rootElement.addContent(stateElement);

            stateElement.setAttribute("id", entry.getKey().toString());

            Element dataSourceState = new Element("data-state");
            dataSourceState.setAttribute("version", String.valueOf(entry.getValue().getVersion()));
            dataSourceState.addContent(entry.getValue().toXmlState());

            stateElement.addContent(dataSourceState);
        }
        return rootElement;
    }

    @Override
    public void loadState(Element state) {
        for (Element element : state.getChildren()) {
            String id = element.getAttributeValue("id");
            if (id == null) {
                continue;
            }

            UUID uuid = UUID.fromString(id);

            Element stateElement = element.getChild("data-state");

            if (stateElement == null || stateElement.getContentSize() == 0) {
                continue;
            }

            int version = Integer.parseInt(stateElement.getAttributeValue("version", "0"));

            Element firstChild = stateElement.getChildren().get(0);

            myStates.put(uuid, new DataSourceState(version, firstChild, null));
        }
    }
}
