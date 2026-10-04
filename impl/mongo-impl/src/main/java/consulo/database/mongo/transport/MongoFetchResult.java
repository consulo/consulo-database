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

import consulo.database.datasource.transport.DataSourceTransportResult;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The first page of a collection: its documents as read, and the top level fields of the page together with a random sample.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public final class MongoFetchResult implements DataSourceTransportResult {
    public static final long UNKNOWN_COUNT = -1;

    private final String myDatabaseName;
    private final String myCollectionName;
    private final List<MongoValue> myDocuments;
    private final List<MongoFieldInfo> myFields;
    private final long myTotalCount;
    private final boolean myTotalCountPrecise;
    private final boolean myHasMore;

    public MongoFetchResult(String databaseName,
                            String collectionName,
                            List<MongoValue> documents,
                            List<MongoFieldInfo> fields,
                            long totalCount,
                            boolean totalCountPrecise,
                            boolean hasMore) {
        myDatabaseName = databaseName;
        myCollectionName = collectionName;
        myDocuments = List.copyOf(documents);
        myFields = List.copyOf(fields);
        myTotalCount = totalCount;
        myTotalCountPrecise = totalCountPrecise;
        myHasMore = hasMore;
    }

    public String getDatabaseName() {
        return myDatabaseName;
    }

    public String getCollectionName() {
        return myCollectionName;
    }

    public List<MongoValue> getDocuments() {
        return myDocuments;
    }

    /**
     * @return the top level fields, {@code _id} first - the columns of the result
     */
    public List<MongoFieldInfo> getFields() {
        return myFields;
    }

    public List<String> getFieldNames() {
        List<String> names = new ArrayList<>(myFields.size());
        for (MongoFieldInfo field : myFields) {
            names.add(field.getName());
        }
        return names;
    }

    /**
     * @return the cell values of a document, positional by {@link #getFields()}, with {@link MongoValues#MISSING} for absent fields
     */
    public @Nullable Object[] getRow(int index) {
        return MongoValueMapper.toRow(myDocuments.get(index), getFieldNames());
    }

    /**
     * @return the number of documents of the collection, or {@link #UNKNOWN_COUNT}
     */
    public long getTotalCount() {
        return myTotalCount;
    }

    /**
     * @return {@code false} when {@link #getTotalCount()} is the estimate of the collection metadata, which may be stale
     */
    public boolean isTotalCountPrecise() {
        return myTotalCountPrecise;
    }

    /**
     * @return whether the collection has documents after this page
     */
    public boolean hasMore() {
        return myHasMore;
    }

    @Override
    public long getAllRowsCount() {
        return myTotalCount == UNKNOWN_COUNT ? myDocuments.size() : myTotalCount;
    }

    @Override
    public long getRowsCount() {
        return myDocuments.size();
    }
}
