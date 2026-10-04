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


package consulo.database.mongo.transport;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.concurrent.ApplicationConcurrency;
import consulo.application.progress.ProgressIndicator;
import consulo.component.ProcessCanceledException;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransport;
import consulo.database.datasource.transport.DataSourceTransportResult;
import consulo.database.mongo.MongoDbDataSourceProvider;
import consulo.database.mongo.localize.MongoLocalize;
import consulo.database.mongo.rt.shared.MongoCollectionInfo;
import consulo.database.mongo.rt.shared.MongoConnectSettings;
import consulo.database.mongo.rt.shared.MongoDocumentShape;
import consulo.database.mongo.rt.shared.MongoErrorKind;
import consulo.database.mongo.rt.shared.MongoFieldShape;
import consulo.database.mongo.rt.shared.MongoFindResult;
import consulo.database.mongo.rt.shared.MongoNode;
import consulo.database.mongo.rt.shared.MongoNodeKind;
import consulo.database.mongo.session.MongoRuntimeException;
import consulo.database.mongo.session.MongoSession;
import consulo.database.mongo.session.MongoSessionService;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.util.concurrent.AsyncResult;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * MongoDB transport over the runtime process, which runs the official sync driver outside the IDE like the JDBC runtime runs a
 * JDBC driver. The runtime of a data source is kept by {@link MongoSessionService} between calls; Test Connection starts a one-shot
 * runtime of its own.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@ExtensionImpl(id = "mongodb", order = "before fake")
public class MongoDataSourceTransport implements DataSourceTransport<MongoState> {
    private static final Logger LOG = Logger.getInstance(MongoDataSourceTransport.class);

    /**
     * The number of documents of the first page.
     */
    public static final int PAGE_SIZE = 100;

    private static final int QUERY_MAX_TIME_MS = 30_000;
    private static final int LIST_MAX_TIME_MS = 30_000;
    private static final int COUNT_MAX_TIME_MS = 10_000;
    private static final int SAMPLE_MAX_TIME_MS = 10_000;

    private final ApplicationConcurrency myConcurrency;

    @Inject
    public MongoDataSourceTransport(ApplicationConcurrency concurrency) {
        myConcurrency = concurrency;
    }

    @Override
    public boolean accept(DataSource dataSource) {
        return dataSource.getProvider() instanceof MongoDbDataSourceProvider;
    }

    /**
     * Pings the server from a one-shot runtime with the properties passed in: in the settings dialog they are not applied yet, so the
     * cached runtime would test the old settings - or keep the unsaved ones.
     */
    @Override
    public void testConnection(ProgressIndicator indicator, Project project, DataSource dataSource, AsyncResult<Void> result) {
        try {
            MongoConnectSettings runtimeSettings = MongoSessionService.readSettings(dataSource).toRuntimeSettings(true);

            try (MongoSession session = MongoSession.start(indicator, dataSource, myConcurrency.getScheduledExecutorService())) {
                session.<@Nullable Void>execute(indicator, client -> {
                    client.testConnection(runtimeSettings);
                    return null;
                });
            }
            result.setDone();
        }
        catch (ProcessCanceledException e) {
            result.rejectWithThrowable(e);
        }
        catch (Throwable e) {
            LOG.info("MongoDB connection test of '" + dataSource.getName() + "' failed: " + e.getMessage());
            result.rejectWithThrowable(e);
        }
    }

    @Override
    public void loadInitialData(ProgressIndicator indicator, Project project, DataSource dataSource, AsyncResult<MongoState> result) {
        try {
            MongoSession session = MongoSessionService.getInstance(project).getSession(indicator, dataSource);

            indicator.setText(MongoLocalize.progressListingDatabases(dataSource.getName()));
            List<String> databases = listDatabases(indicator, session, dataSource);

            MongoState state = new MongoState();
            for (String database : databases) {
                indicator.checkCanceled();
                indicator.setText(MongoLocalize.progressListingCollections(dataSource.getName(), database));

                state.addDatabase(new MongoDatabaseState(database, listCollections(indicator, session, database)));
            }

            result.setDone(state);
        }
        catch (ProcessCanceledException e) {
            result.rejectWithThrowable(e);
        }
        catch (Throwable e) {
            LOG.warn("Failed to load MongoDB structure of '" + dataSource.getName() + "'", e);
            result.rejectWithThrowable(e);
        }
    }

    /**
     * Fetches the first {@link #PAGE_SIZE} documents in {@code _id} order, and samples the top level fields from them together with
     * the shapes of {@link MongoSchemaSampler#SAMPLE_SIZE} random documents.
     */
    @Override
    public void fetchData(ProgressIndicator indicator,
                          Project project,
                          DataSource dataSource,
                          String databaseName,
                          String childId,
                          AsyncResult<DataSourceTransportResult> result) {
        try {
            MongoSession session = MongoSessionService.getInstance(project).getSession(indicator, dataSource);

            indicator.setText(MongoLocalize.progressFetchingDocuments(databaseName, childId));

            // one more document than the page tells whether a next page exists. The runtime ends the sort with _id, as paging with
            // skip needs a total order
            MongoFindResult found = session.execute(indicator,
                client -> client.find(databaseName, childId, "", "", 0, PAGE_SIZE + 1, QUERY_MAX_TIME_MS));

            List<MongoValue> documents = decodeDocuments(indicator, found);
            // the runtime stops early at its response budget - there are more documents then, too
            boolean hasMore = documents.size() > PAGE_SIZE || found.isTruncated();
            if (documents.size() > PAGE_SIZE) {
                documents = new ArrayList<>(documents.subList(0, PAGE_SIZE));
            }

            MongoSchemaSampler sampler = new MongoSchemaSampler();
            for (MongoValue document : documents) {
                sampler.add(document);
            }

            long totalCount;
            boolean totalCountPrecise;
            if (hasMore) {
                indicator.checkCanceled();
                indicator.setText(MongoLocalize.progressSamplingFields(databaseName, childId));
                addSample(indicator, session, sampler, databaseName, childId);

                indicator.checkCanceled();
                // the estimate comes from the collection metadata: it may be stale, so it is never precise
                long estimate = estimateCount(indicator, session, databaseName, childId);
                totalCount = estimate > documents.size() ? estimate : MongoFetchResult.UNKNOWN_COUNT;
                totalCountPrecise = false;
            }
            else {
                totalCount = documents.size();
                totalCountPrecise = true;
            }

            result.setDone(new MongoFetchResult(databaseName,
                childId,
                documents,
                sampler.getFields(),
                totalCount,
                totalCountPrecise,
                hasMore));
        }
        catch (ProcessCanceledException e) {
            result.rejectWithThrowable(e);
        }
        catch (Throwable e) {
            LOG.warn("Failed to fetch MongoDB documents of '" + dataSource.getName() + "'", e);
            result.rejectWithThrowable(e);
        }
    }

    @Override
    public Class<MongoState> getStateClass() {
        return MongoState.class;
    }

    @Override
    public int getStateVersion() {
        return 1;
    }

    /**
     * Decodes the documents one by one, dropping each wire document once decoded - a page is held twice only one document at a time.
     */
    private static List<MongoValue> decodeDocuments(ProgressIndicator indicator, MongoFindResult found) {
        if (!found.isSetDocuments()) {
            return new ArrayList<>();
        }

        List<MongoNode> nodes = found.getDocuments();
        List<MongoValue> documents = new ArrayList<>(nodes.size());
        for (int i = 0; i < nodes.size(); i++) {
            indicator.checkCanceled();

            documents.add(MongoNodeDecoder.decodeDocument(nodes.get(i)));
            nodes.set(i, null);
        }
        return documents;
    }

    /**
     * @return the databases the user may read. When none can be listed, the database of the data source settings
     */
    private static List<String> listDatabases(ProgressIndicator indicator, MongoSession session, DataSource dataSource) {
        List<String> databases = new ArrayList<>();
        @Nullable MongoRuntimeException unauthorized = null;
        try {
            databases.addAll(session.execute(indicator, client -> client.listDatabases(LIST_MAX_TIME_MS)));
        }
        catch (MongoRuntimeException e) {
            if (e.getKind() != MongoErrorKind.UNAUTHORIZED) {
                throw e;
            }
            unauthorized = e;
        }

        if (databases.isEmpty()) {
            String databaseName = dataSource.getValueWithDefault(GenericPropertyKeys.DATABASE_NAME);
            if (databaseName != null && !databaseName.isBlank()) {
                return List.of(databaseName.trim());
            }

            if (unauthorized != null) {
                throw unauthorized;
            }
        }
        return databases;
    }

    /**
     * The runtime falls back to the authorized collection names - typed as collections - for a user who may not list collections,
     * and to none when that is not authorized either.
     */
    private static List<MongoCollectionState> listCollections(ProgressIndicator indicator, MongoSession session, String database) {
        List<MongoCollectionInfo> infos = session.execute(indicator, client -> client.listCollections(database, LIST_MAX_TIME_MS));

        List<MongoCollectionState> collections = new ArrayList<>(infos.size());
        for (MongoCollectionInfo info : infos) {
            String name = info.getName();
            if (name == null) {
                continue;
            }

            String type = info.getType();
            collections.add(new MongoCollectionState(name, type == null || type.isEmpty() ? MongoCollectionState.TYPE_COLLECTION : type));
        }

        collections.sort(Comparator.comparing(MongoCollectionState::getName));
        return collections;
    }

    /**
     * Adds the shapes of random documents ({@code $sample}), read in the runtime - the IDE receives only their top level field names
     * and types. A failure, for example a missing privilege, is not an error: the fields seen so far stay.
     */
    private static void addSample(ProgressIndicator indicator,
                                  MongoSession session,
                                  MongoSchemaSampler sampler,
                                  String databaseName,
                                  String collectionName) {
        List<MongoDocumentShape> shapes;
        try {
            shapes = session.execute(indicator,
                client -> client.sample(databaseName, collectionName, MongoSchemaSampler.SAMPLE_SIZE, SAMPLE_MAX_TIME_MS));
        }
        catch (MongoRuntimeException e) {
            LOG.info("Cannot sample fields of " + databaseName + "." + collectionName + ": " + e.getMessage());
            return;
        }

        for (MongoDocumentShape shape : shapes) {
            indicator.checkCanceled();

            MongoValue id = shape.isSetId() ? MongoNodeDecoder.decode(shape.getId()) : null;

            List<MongoSchemaSampler.FieldType> fields = new ArrayList<>(shape.getFieldsSize());
            if (shape.isSetFields()) {
                for (MongoFieldShape field : shape.getFields()) {
                    MongoNodeKind kind = field.getKind();
                    if (kind == MongoNodeKind.MISSING || field.getName() == null) {
                        continue;
                    }
                    fields.add(new MongoSchemaSampler.FieldType(field.getName(), MongoNodeDecoder.typeName(kind, field.getTypeName())));
                }
            }
            sampler.addShape(id, fields);
        }
    }

    private static long estimateCount(ProgressIndicator indicator, MongoSession session, String databaseName, String collectionName) {
        try {
            return session.execute(indicator, client -> client.estimatedCount(databaseName, collectionName, COUNT_MAX_TIME_MS));
        }
        catch (MongoRuntimeException e) {
            // for example a view on an older server
            LOG.info("Cannot estimate document count of " + databaseName + "." + collectionName + ": " + e.getMessage());
            return MongoFetchResult.UNKNOWN_COUNT;
        }
    }
}
