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

import com.mongodb.MongoServerException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListCollectionNamesIterable;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.EstimatedDocumentCountOptions;
import consulo.database.mongo.rt.shared.MongoCollectionInfo;
import consulo.database.mongo.rt.shared.MongoConnectSettings;
import consulo.database.mongo.rt.shared.MongoDocumentShape;
import consulo.database.mongo.rt.shared.MongoErrorKind;
import consulo.database.mongo.rt.shared.MongoExecutor;
import consulo.database.mongo.rt.shared.MongoExecutorConstants;
import consulo.database.mongo.rt.shared.MongoFailError;
import consulo.database.mongo.rt.shared.MongoFieldShape;
import consulo.database.mongo.rt.shared.MongoFindResult;
import consulo.database.mongo.rt.shared.MongoHelloResult;
import consulo.database.mongo.rt.shared.MongoNode;
import org.apache.thrift.transport.TSocket;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonValue;
import org.bson.RawBsonDocument;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The handler of one connection: the server creates one per accepted socket, and one thread serves it, so the handshake state
 * needs no synchronization. Every call but {@link #hello} fails with {@link MongoErrorKind#NOT_AUTHENTICATED} until
 * {@link #hello} got the client token.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
final class MongoExecutorImpl implements MongoExecutor.Iface {
    /**
     * The BSON size of the documents of one {@link #find} answer. The answer is a tree of objects many times larger, held twice
     * in the IDE while decoding - the budget keeps both processes far from their heap limits.
     */
    static final int RESPONSE_BUDGET_BYTES = 8 * 1024 * 1024;

    /**
     * The number of fields of all shapes of one {@link #sample} answer: a collection with thousands of top level fields would
     * otherwise give millions of shapes.
     */
    static final int SAMPLE_FIELD_BUDGET = 100_000;

    private static final String ID_FIELD = "_id";
    private static final String COLLECTION_TYPE = "collection";

    @FunctionalInterface
    private interface Call<T extends @Nullable Object> {
        T run() throws Exception;
    }

    private final MongoRuntimeState myState;
    private final @Nullable TSocket mySocket;

    private boolean myAuthenticated;
    private boolean myRejected;

    MongoExecutorImpl(MongoRuntimeState state, @Nullable TSocket socket) {
        myState = state;
        mySocket = socket;
    }

    @Override
    public MongoHelloResult hello(String clientToken) throws MongoFailError {
        if (myRejected || !myState.isValidClientToken(clientToken)) {
            // a connection which sent a wrong token stays rejected: guessing needs a new connection each time
            myRejected = true;
            myAuthenticated = false;
            throw MongoErrors.fail(MongoErrorKind.NOT_AUTHENTICATED, "Not authenticated");
        }

        myAuthenticated = true;
        if (mySocket != null) {
            // the read timeout only drops connections which never authenticate - an authenticated one may idle in a pool
            mySocket.setTimeout(0);
        }
        return new MongoHelloResult(MongoExecutorConstants.PROTOCOL_VERSION, myState.getServerToken());
    }

    @Override
    public void connect(MongoConnectSettings settings) throws MongoFailError {
        call(() -> {
            myState.connect(settings);
            return null;
        });
    }

    @Override
    public void testConnection(MongoConnectSettings settings) throws MongoFailError {
        call(() -> {
            try (MongoClient client = MongoClientSettingsFactory.createClient(settings, true)) {
                ping(client, MongoClientSettingsFactory.getAuthSource(settings));
            }
            return null;
        });
    }

    @Override
    public void ping() throws MongoFailError {
        call(() -> {
            ping(myState.getClient(), myState.getAuthSource());
            return null;
        });
    }

    @Override
    public List<String> listDatabases(int maxTimeMs) throws MongoFailError {
        return call(() -> {
            ListDatabasesIterable<BsonDocument> iterable = myState.getClient()
                .listDatabases(BsonDocument.class)
                .nameOnly(true)
                .authorizedDatabasesOnly(true);
            if (maxTimeMs > 0) {
                iterable.maxTime(maxTimeMs, TimeUnit.MILLISECONDS);
            }

            List<String> names = new ArrayList<>();
            try (MongoCursor<BsonDocument> cursor = iterable.iterator()) {
                while (cursor.hasNext()) {
                    @Nullable String name = getString(cursor.next(), "name");
                    if (name != null) {
                        names.add(name);
                    }
                }
            }
            return names;
        });
    }

    @Override
    public List<MongoCollectionInfo> listCollections(String database, int maxTimeMs) throws MongoFailError {
        return call(() -> {
            MongoDatabase mongoDatabase = getDatabase(database);

            List<MongoCollectionInfo> collections = new ArrayList<>();
            try {
                ListCollectionsIterable<BsonDocument> iterable = mongoDatabase.listCollections(BsonDocument.class);
                if (maxTimeMs > 0) {
                    iterable.maxTime(maxTimeMs, TimeUnit.MILLISECONDS);
                }

                try (MongoCursor<BsonDocument> cursor = iterable.iterator()) {
                    while (cursor.hasNext()) {
                        BsonDocument info = cursor.next();

                        @Nullable String name = getString(info, "name");
                        if (name != null) {
                            @Nullable String type = getString(info, "type");
                            collections.add(new MongoCollectionInfo(name, type == null ? COLLECTION_TYPE : type));
                        }
                    }
                }
            }
            catch (MongoServerException e) {
                if (!MongoErrors.isUnauthorized(e)) {
                    throw e;
                }

                // a user who may read some collections only - the names it is authorized for, without their types
                collections.clear();
                try {
                    ListCollectionNamesIterable names = mongoDatabase.listCollectionNames().authorizedCollections(true);
                    if (maxTimeMs > 0) {
                        names.maxTime(maxTimeMs, TimeUnit.MILLISECONDS);
                    }

                    for (String name : names) {
                        collections.add(new MongoCollectionInfo(name, COLLECTION_TYPE));
                    }
                }
                catch (MongoServerException again) {
                    if (!MongoErrors.isUnauthorized(again)) {
                        throw again;
                    }
                    collections.clear();
                }
            }
            return collections;
        });
    }

    @Override
    public MongoFindResult find(String database,
                                String collection,
                                String filterJson,
                                String sortJson,
                                int skip,
                                int limit,
                                int maxTimeMs) throws MongoFailError {
        return call(() -> {
            if (limit <= 0) {
                throw MongoErrors.fail(MongoErrorKind.INVALID_QUERY, "The limit must be positive: " + limit);
            }
            if (skip < 0) {
                throw MongoErrors.fail(MongoErrorKind.INVALID_QUERY, "The skip must not be negative: " + skip);
            }

            BsonDocument filter = parseJson(filterJson, "filter");
            BsonDocument sort = parseJson(sortJson, "sort");
            // skip/limit paging needs a total order
            if (!sort.containsKey(ID_FIELD)) {
                sort.append(ID_FIELD, new BsonInt32(1));
            }

            MongoCollection<RawBsonDocument> mongoCollection = getCollection(database, collection);

            myState.acquireQuerySlot(maxTimeMs);
            try {
                FindIterable<RawBsonDocument> iterable = mongoCollection.find(filter)
                    .sort(sort)
                    .skip(skip)
                    .limit(limit)
                    .batchSize(limit)
                    .allowDiskUse(true);
                if (maxTimeMs > 0) {
                    iterable.maxTime(maxTimeMs, TimeUnit.MILLISECONDS);
                }

                List<MongoNode> documents = new ArrayList<>(Math.min(limit, 1024));
                boolean truncated = false;
                long size = 0;
                try (MongoCursor<RawBsonDocument> cursor = iterable.iterator()) {
                    while (cursor.hasNext()) {
                        RawBsonDocument document = cursor.next();

                        int documentSize = document.getByteBuffer().remaining();
                        if (!documents.isEmpty() && size + documentSize > RESPONSE_BUDGET_BYTES) {
                            truncated = true;
                            break;
                        }

                        size += documentSize;
                        documents.add(MongoNodeEncoder.encode(null, document));
                    }
                }
                return new MongoFindResult(documents, truncated);
            }
            finally {
                myState.releaseQuerySlot();
            }
        });
    }

    @Override
    public List<MongoDocumentShape> sample(String database, String collection, int size, int maxTimeMs) throws MongoFailError {
        return call(() -> {
            if (size <= 0) {
                throw MongoErrors.fail(MongoErrorKind.INVALID_QUERY, "The sample size must be positive: " + size);
            }

            MongoCollection<RawBsonDocument> mongoCollection = getCollection(database, collection);
            List<BsonDocument> pipeline = List.of(new BsonDocument("$sample", new BsonDocument("size", new BsonInt32(size))));

            myState.acquireQuerySlot(maxTimeMs);
            try {
                AggregateIterable<RawBsonDocument> iterable = mongoCollection.aggregate(pipeline).allowDiskUse(true).batchSize(size);
                if (maxTimeMs > 0) {
                    iterable.maxTime(maxTimeMs, TimeUnit.MILLISECONDS);
                }

                List<MongoDocumentShape> shapes = new ArrayList<>(Math.min(size, 1024));
                int fieldCount = 0;
                try (MongoCursor<RawBsonDocument> cursor = iterable.iterator()) {
                    while (cursor.hasNext() && fieldCount < SAMPLE_FIELD_BUDGET) {
                        RawBsonDocument document = cursor.next();

                        List<MongoFieldShape> fields = new ArrayList<>();
                        @Nullable MongoNode id = null;
                        for (Map.Entry<String, BsonValue> entry : document.entrySet()) {
                            String name = entry.getKey();
                            BsonValue value = entry.getValue();

                            fields.add(MongoNodeEncoder.shape(name, value));
                            if (id == null && ID_FIELD.equals(name)) {
                                id = MongoNodeEncoder.encode(null, value);
                            }
                        }

                        MongoDocumentShape shape = new MongoDocumentShape(fields);
                        if (id != null) {
                            shape.setId(id);
                        }
                        shapes.add(shape);
                        fieldCount += fields.size();
                    }
                }
                return shapes;
            }
            finally {
                myState.releaseQuerySlot();
            }
        });
    }

    @Override
    public long estimatedCount(String database, String collection, int maxTimeMs) throws MongoFailError {
        return call(() -> {
            EstimatedDocumentCountOptions options = new EstimatedDocumentCountOptions();
            if (maxTimeMs > 0) {
                options.maxTime(maxTimeMs, TimeUnit.MILLISECONDS);
            }
            return getCollection(database, collection).estimatedDocumentCount(options);
        });
    }

    @Override
    public long countDocuments(String database, String collection, String filterJson, int maxTimeMs) throws MongoFailError {
        return call(() -> {
            BsonDocument filter = parseJson(filterJson, "filter");

            CountOptions options = new CountOptions();
            if (maxTimeMs > 0) {
                options.maxTime(maxTimeMs, TimeUnit.MILLISECONDS);
            }
            return getCollection(database, collection).countDocuments(filter, options);
        });
    }

    @Override
    public void shutdown() {
        if (myAuthenticated) {
            myState.exit();
        }
    }

    private <T extends @Nullable Object> T call(Call<T> call) throws MongoFailError {
        if (!myAuthenticated) {
            throw MongoErrors.fail(MongoErrorKind.NOT_AUTHENTICATED, "Not authenticated: hello() must be the first call");
        }

        try {
            return call.run();
        }
        catch (Throwable e) {
            // also errors: a too deep document or an out of memory must answer the call, not end the connection
            throw MongoErrors.toFailError(e);
        }
    }

    private static void ping(MongoClient client, String authSource) {
        client.getDatabase(authSource).runCommand(new BsonDocument("ping", new BsonInt32(1)));
    }

    private MongoDatabase getDatabase(String database) throws MongoFailError {
        try {
            return myState.getClient().getDatabase(database);
        }
        catch (IllegalArgumentException e) {
            throw MongoErrors.fail(MongoErrorKind.INVALID_QUERY, MongoErrors.getMessage(e), e);
        }
    }

    private MongoCollection<RawBsonDocument> getCollection(String database, String collection) throws MongoFailError {
        MongoDatabase mongoDatabase = getDatabase(database);
        try {
            return mongoDatabase.getCollection(collection, RawBsonDocument.class);
        }
        catch (IllegalArgumentException e) {
            throw MongoErrors.fail(MongoErrorKind.INVALID_QUERY, MongoErrors.getMessage(e), e);
        }
    }

    /**
     * @param json relaxed JSON in shell syntax, for example {@code {_id: ObjectId("...")}}; blank means {@code {}}
     */
    private static BsonDocument parseJson(@Nullable String json, String what) throws MongoFailError {
        if (json == null || json.isBlank()) {
            return new BsonDocument();
        }

        try {
            return BsonDocument.parse(json);
        }
        catch (RuntimeException e) {
            throw MongoErrors.fail(MongoErrorKind.INVALID_QUERY, "Invalid " + what + ": " + MongoErrors.getMessage(e), e);
        }
    }

    private static @Nullable String getString(BsonDocument document, String key) {
        @Nullable BsonValue value = document.get(key);
        return value != null && value.isString() ? value.asString().getValue() : null;
    }
}
