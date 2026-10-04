/*
 * Copyright 2013-2021 consulo.io
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

package consulo.database.datasource.jdbc.transport;

import consulo.database.datasource.transport.DataSourceTransportResult;
import consulo.database.jdbc.rt.shared.JdbcColumnMeta;
import consulo.database.jdbc.rt.shared.JdbcExecutionResult;
import consulo.database.jdbc.rt.shared.JdbcQueryRow;
import consulo.database.jdbc.rt.shared.JdbcResultSet;
import consulo.database.jdbc.rt.shared.JdbcStatementResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The decoded result of one execution: every result set (columns and rows), the update counts of the statements which returned
 * no rows, and the warnings.
 *
 * @author VISTALL
 * @since 09/12/2021
 */
@NullMarked
public class JdbcQueryResultWrapper implements DataSourceTransportResult {
    private final List<JdbcResultSetData> myResultSets;
    private final List<Long> myUpdateCounts;
    private final List<String> myWarnings;
    private final long myAllRowsCount;
    private final @Nullable String myQuery;

    /**
     * The count of all rows is the count of the rows of the first result set, and the execution cannot be run again
     */
    public JdbcQueryResultWrapper(JdbcExecutionResult executionResult) {
        this(executionResult, -1, null);
    }

    /**
     * @param allRowsCount the count of all rows, for example from {@code SELECT COUNT(*)}; less than 0 when unknown
     * @param query        the statements which gave the result, to run them again; null when the result cannot be fetched again
     */
    public JdbcQueryResultWrapper(JdbcExecutionResult executionResult, long allRowsCount, @Nullable String query) {
        List<JdbcResultSetData> resultSets = new ArrayList<>();
        List<Long> updateCounts = new ArrayList<>();

        List<JdbcStatementResult> results = executionResult.isSetResults() ? executionResult.getResults() : List.of();
        for (JdbcStatementResult result : results) {
            if (result.isSetResultSet()) {
                resultSets.add(decodeResultSet(resultSets.size(), result.getResultSet()));
            }
            else if (result.isSetUpdateCount()) {
                updateCounts.add(result.getUpdateCount());
            }
        }

        myResultSets = Collections.unmodifiableList(resultSets);
        myUpdateCounts = Collections.unmodifiableList(updateCounts);
        myWarnings = executionResult.isSetWarnings() ? List.copyOf(executionResult.getWarnings()) : List.of();
        myAllRowsCount = allRowsCount;
        myQuery = query;
    }

    private JdbcQueryResultWrapper(List<JdbcResultSetData> resultSets,
                                   List<Long> updateCounts,
                                   List<String> warnings,
                                   long allRowsCount,
                                   @Nullable String query) {
        myResultSets = resultSets;
        myUpdateCounts = updateCounts;
        myWarnings = warnings;
        myAllRowsCount = allRowsCount;
        myQuery = query;
    }

    private static JdbcResultSetData decodeResultSet(int index, JdbcResultSet resultSet) {
        List<JdbcResultColumn> columns = new ArrayList<>();
        List<JdbcColumnMeta> columnMetas = resultSet.isSetColumns() ? resultSet.getColumns() : List.of();
        for (int i = 0; i < columnMetas.size(); i++) {
            columns.add(JdbcValueDecoder.decodeColumn(i, columnMetas.get(i)));
        }

        List<JdbcResultRow> rows = new ArrayList<>();
        List<JdbcQueryRow> queryRows = resultSet.isSetRows() ? resultSet.getRows() : List.of();
        for (JdbcQueryRow queryRow : queryRows) {
            rows.add(JdbcValueDecoder.decodeRow(queryRow));
        }

        return new JdbcResultSetData(index,
            Collections.unmodifiableList(columns),
            Collections.unmodifiableList(rows),
            resultSet.isHasMore());
    }

    /**
     * @return one result per result set, each without the update counts, then one result with the update counts if there are
     * any; this result when there is no result set. When the statements also changed data or the schema, the parts cannot be
     * fetched again: they have no query
     */
    public List<JdbcQueryResultWrapper> splitByResultSet() {
        if (myResultSets.isEmpty() || (myResultSets.size() == 1 && myUpdateCounts.isEmpty())) {
            return List.of(this);
        }

        // statements which change data or the schema must not run again from a reload of one of their result sets
        String query = myUpdateCounts.isEmpty() ? myQuery : null;
        List<JdbcQueryResultWrapper> parts = new ArrayList<>(myResultSets.size());
        for (JdbcResultSetData resultSet : myResultSets) {
            parts.add(new JdbcQueryResultWrapper(List.of(resultSet), List.of(), myWarnings, -1, query));
        }
        if (!myUpdateCounts.isEmpty()) {
            // the statements without a result set get a part of their own, which shows how many rows they changed
            parts.add(new JdbcQueryResultWrapper(List.of(), myUpdateCounts, myWarnings, -1, null));
        }
        return parts;
    }

    /**
     * @return true when the execution returned a result set
     */
    public boolean hasResultSet() {
        return !myResultSets.isEmpty();
    }

    /**
     * @return every result set, in execution order
     */
    public List<JdbcResultSetData> getResultSets() {
        return myResultSets;
    }

    /**
     * @return the columns of the first result set, empty when there is none
     */
    public List<JdbcResultColumn> getColumns() {
        return myResultSets.isEmpty() ? List.of() : myResultSets.get(0).columns();
    }

    /**
     * @return the rows of the first result set, empty when there is none
     */
    public List<JdbcResultRow> getRows() {
        return myResultSets.isEmpty() ? List.of() : myResultSets.get(0).rows();
    }

    /**
     * @return true when the first result set has more rows than were returned
     */
    public boolean hasMore() {
        return !myResultSets.isEmpty() && myResultSets.get(0).hasMore();
    }

    /**
     * @return how many result sets the execution returned
     */
    public int getResultSetCount() {
        return myResultSets.size();
    }

    /**
     * @return the update counts of the statements which returned no result set, in execution order
     */
    public List<Long> getUpdateCounts() {
        return myUpdateCounts;
    }

    public List<String> getWarnings() {
        return myWarnings;
    }

    /**
     * @return the statements which gave the result, to run them again; null when the result cannot be fetched again
     */
    public @Nullable String getQuery() {
        return myQuery;
    }

    @Override
    public long getAllRowsCount() {
        return myAllRowsCount < 0 ? getRowsCount() : myAllRowsCount;
    }

    @Override
    public long getRowsCount() {
        return getRows().size();
    }
}
