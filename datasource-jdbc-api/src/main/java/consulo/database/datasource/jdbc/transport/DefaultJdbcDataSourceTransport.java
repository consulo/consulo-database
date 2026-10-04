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

package consulo.database.datasource.jdbc.transport;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.progress.ProgressIndicator;
import consulo.component.ProcessCanceledException;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.jdbc.provider.JdbcDataSourceProvider;
import consulo.database.datasource.jdbc.provider.impl.*;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.DataSourceTransport;
import consulo.database.datasource.transport.DataSourceTransportManager;
import consulo.database.datasource.transport.DataSourceTransportResult;
import consulo.database.jdbc.rt.shared.JdbcColum;
import consulo.database.jdbc.rt.shared.JdbcExecutionResult;
import consulo.database.jdbc.rt.shared.JdbcExecutor;
import consulo.database.jdbc.rt.shared.JdbcTable;
import consulo.database.jdbc.rt.shared.JdbcTablePrimaryKey;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.util.concurrent.AsyncResult;
import consulo.util.lang.StringUtil;
import consulo.util.lang.function.ThrowableConsumer;
import jakarta.annotation.Nonnull;
import org.apache.thrift.TException;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
@ExtensionImpl(id = "default", order = "before fake")
public class DefaultJdbcDataSourceTransport implements DataSourceTransport<JdbcState> {
    private static final Logger LOG = Logger.getInstance(DefaultJdbcDataSourceTransport.class);

    /**
     * How many rows of a table {@link #fetchData} fetches
     */
    public static final int PAGE_SIZE = 500;

    @Override
    public boolean accept(@Nonnull DataSource dataSource) {
        return dataSource.getProvider() instanceof JdbcDataSourceProvider;
    }

    private <T> void safeCall(@Nonnull ProgressIndicator indicator, @Nonnull DataSource dataSource, @Nonnull AsyncResult<T> result, @Nonnull ThrowableConsumer<JdbcSession, Throwable> sessionConsumer) {
        try (JdbcSession session = new JdbcSession(indicator, dataSource)) {
            sessionConsumer.consume(session);
        }
        catch (ProcessCanceledException e) {
            result.rejectWithThrowable(e);
        }
        catch (Throwable e) {
            LOG.warn(e);

            result.rejectWithThrowable(e);
        }
    }

    @Override
    public void testConnection(@Nonnull ProgressIndicator indicator, @Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull AsyncResult<Void> result) {
        safeCall(indicator, dataSource, result, session ->
        {
            Boolean valid = session.execute(JdbcExecutor.Client::testConnection);

            if (valid) {
                result.setDone();
            }
            else {
                result.rejectWithThrowable(new Error("Failed"));
            }
        });
    }

    @Override
    public void loadInitialData(@Nonnull ProgressIndicator indicator,
                                @Nonnull Project project,
                                @Nonnull DataSource dataSource,
                                @Nonnull AsyncResult<JdbcState> result) {
        String databaseName = StringUtil.nullize(dataSource.getValueWithDefault(GenericPropertyKeys.DATABASE_NAME));

        safeCall(indicator, dataSource, result, session -> {
            JdbcState state = new JdbcState();

            List<String> databases = session.execute(JdbcExecutor.Client::listDatabases);

            if (databases.isEmpty() && databaseName != null) {
                databases = List.of(databaseName);
            }

            for (String database : databases) {
                indicator.setText("Processing '" + dataSource.getName() + ":" + database + "'");

                List<JdbcTable> jdbcTables = session.execute(client -> client.listTables(database));

                state.addDatabase(database, new JdbcDatabaseState(database, convertToTables(jdbcTables)));
            }

            result.setDone(state);
        });
    }

    @Override
    public void fetchData(@Nonnull ProgressIndicator indicator,
                          @Nonnull Project project,
                          @Nonnull DataSource dataSource,
                          @Nonnull String databaseName,
                          @Nonnull String childId,
                          @Nonnull AsyncResult<DataSourceTransportResult> result) {
        safeCall(indicator, dataSource, result, session ->
            result.setDone(fetchTablePage(session, project, dataSource, databaseName, childId, 0, PAGE_SIZE).result()));
    }

    /**
     * Fetches a page of the rows of a table, together with the count of all its rows, in one session. The rows are ordered by the
     * primary key, when the cached state of the data source knows it, so that the pages do not overlap. Blocks the calling thread,
     * which must not be the UI thread.
     *
     * @param childId  {@link JdbcTableState#getNameWithScheme()}
     * @param offset   the index of the first row, counting from 0; less than 0 counts from the end, so {@code -pageSize} gives the
     *                 last page. An offset past the last row - rows deleted meanwhile - gives the last page
     * @param pageSize the maximum count of rows; less than 1 fetches every row from the offset on
     */
    public static JdbcTablePage fetchTablePage(ProgressIndicator indicator,
                                               Project project,
                                               DataSource dataSource,
                                               String databaseName,
                                               String childId,
                                               int offset,
                                               int pageSize) throws Exception {
        try (JdbcSession session = new JdbcSession(indicator, dataSource)) {
            return fetchTablePage(session, project, dataSource, databaseName, childId, offset, pageSize);
        }
    }

    private static JdbcTablePage fetchTablePage(JdbcSession session,
                                                Project project,
                                                DataSource dataSource,
                                                String databaseName,
                                                String childId,
                                                int offset,
                                                int pageSize) throws TException {
        session.execute(client -> {
            client.setDatabase(databaseName);
            return null;
        });

        String quote = getIdentifierQuoteString(dataSource);
        JdbcTableState tableState = findTableState(project, dataSource, databaseName, childId);
        String tableReference = buildTableReference(tableState, childId, quote);

        String countQuery = "SELECT COUNT(*) FROM " + tableReference;

        JdbcExecutionResult countResult = session.execute(client -> client.execute(countQuery, List.of(), 1, 0));

        long rowsCount = readCount(new JdbcQueryResultWrapper(countResult));

        int maxRows = Math.max(pageSize, 0);
        long requestedStart = offset < 0 ? rowsCount + offset : offset;
        long start;
        if (requestedStart >= rowsCount) {
            // past the end, as rows were deleted meanwhile - the last page instead
            start = maxRows > 0 ? Math.max(0, rowsCount - maxRows) : 0;
        }
        else {
            start = Math.max(0, requestedStart);
        }
        int startRow = (int) Math.min(start, Integer.MAX_VALUE);

        String query = "SELECT * FROM " + tableReference + buildOrderBy(tableState, quote);

        JdbcExecutionResult queryResult = session.execute(client -> client.execute(query, List.of(), maxRows, startRow));

        return new JdbcTablePage(new JdbcQueryResultWrapper(queryResult, rowsCount, null), startRow);
    }

    private static long readCount(JdbcQueryResultWrapper countResult) {
        List<JdbcResultRow> rows = countResult.getRows();
        if (rows.isEmpty() || countResult.getColumns().isEmpty()) {
            return 0;
        }

        Object value = rows.get(0).getValue(0);
        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof String text) {
            try {
                return Long.parseLong(text.trim());
            }
            catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Builds quoted table reference for {@code childId}, which is {@link JdbcTableState#getNameWithScheme()}
     * ({@code scheme.name} or {@code name}).
     * Each part quoted separately, since table and scheme names can contain dots, spaces, quotes or be in mixed case
     */
    private static String buildTableReference(@Nullable JdbcTableState tableState, String childId, String quote) {
        if (tableState != null && tableState.getName() != null) {
            String scheme = tableState.getScheme();
            if (StringUtil.isEmpty(scheme)) {
                return quoteIdentifier(tableState.getName(), quote);
            }
            return quoteIdentifier(scheme, quote) + "." + quoteIdentifier(tableState.getName(), quote);
        }

        // no cached table state - split name like JdbcTableState#getNameWithScheme() joined it
        int dotIndex = childId.indexOf('.');
        if (dotIndex > 0 && dotIndex < childId.length() - 1) {
            return quoteIdentifier(childId.substring(0, dotIndex), quote) + "." + quoteIdentifier(childId.substring(dotIndex + 1), quote);
        }
        return quoteIdentifier(childId, quote);
    }

    /**
     * @return {@code " ORDER BY "} and the quoted primary key columns in key order, or an empty string when the primary key is
     * unknown - the rows then come in the order the database gives, which may differ between two pages
     */
    private static String buildOrderBy(@Nullable JdbcTableState tableState, String quote) {
        if (tableState == null) {
            return "";
        }

        List<JdbcPrimaryKeyState> primaryKeys = new ArrayList<>();
        for (JdbcPrimaryKeyState primaryKey : tableState.getPrimaryKeys()) {
            if (!StringUtil.isEmpty(primaryKey.getColumnName())) {
                primaryKeys.add(primaryKey);
            }
        }

        if (primaryKeys.isEmpty()) {
            return "";
        }

        primaryKeys.sort(Comparator.comparingInt(JdbcPrimaryKeyState::getKeySeq));

        StringBuilder builder = new StringBuilder(" ORDER BY ");
        for (int i = 0; i < primaryKeys.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(quoteIdentifier(primaryKeys.get(i).getColumnName(), quote));
        }
        return builder.toString();
    }

    /**
     * {@link java.sql.DatabaseMetaData#getIdentifierQuoteString()} is not reachable via rt protocol, use known dialects
     */
    private static String getIdentifierQuoteString(DataSource dataSource) {
        switch (dataSource.getProvider().getId()) {
            case "mysql":
            case "mariadb":
                // backtick works with and without ANSI_QUOTES sql mode
                return "`";
            default:
                // SQL standard (PostgreSQL etc.)
                return "\"";
        }
    }

    private static String quoteIdentifier(String identifier, String quote) {
        return quote + identifier.replace(quote, quote + quote) + quote;
    }

    private static @Nullable JdbcTableState findTableState(Project project,
                                                           DataSource dataSource,
                                                           @Nullable String dbName,
                                                           @Nullable String childId) {
        if (dbName == null || childId == null) {
            return null;
        }

        JdbcState dataState = DataSourceTransportManager.getInstance(project).getDataState(dataSource);
        JdbcDatabaseState databaseState = dataState == null ? null : dataState.getDatabases().get(dbName);
        if (databaseState == null) {
            return null;
        }
        return databaseState.getTablesState().findTable(childId);
    }

    @Override
    public void runQuery(@Nonnull ProgressIndicator indicator, @Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull String query, @Nonnull AsyncResult<DataSourceTransportResult> result) {
        safeCall(indicator, dataSource, result, session -> result.setDone(executeQuery(session, query)));
    }

    /**
     * Runs any statements - queries, DML or DDL - and fetches every row of their result sets. Blocks the calling thread, which must
     * not be the UI thread.
     *
     * @return the result, which can run the statements again ({@link JdbcQueryResultWrapper#getQuery()})
     */
    public static JdbcQueryResultWrapper executeQuery(ProgressIndicator indicator, DataSource dataSource, String query) throws Exception {
        try (JdbcSession session = new JdbcSession(indicator, dataSource)) {
            return executeQuery(session, query);
        }
    }

    private static JdbcQueryResultWrapper executeQuery(JdbcSession session, String query) throws TException {
        // all rows of a query are fetched, as the result of a query is shown as one page
        JdbcExecutionResult executionResult = session.execute(client -> client.execute(query, List.of(), 0, 0));

        return new JdbcQueryResultWrapper(executionResult, -1, query);
    }

    @Nonnull
    private static JdbcTablesState convertToTables(List<JdbcTable> jdbcTables) {
        JdbcTablesState state = new JdbcTablesState();
        for (JdbcTable jdbcTable : jdbcTables) {
            JdbcTableState tableState = new JdbcTableState();
            tableState.setName(jdbcTable.getName());
            tableState.setType(jdbcTable.getType());
            tableState.setScheme(jdbcTable.getScheme());

            for (JdbcColum colum : jdbcTable.getColums()) {
                tableState.addColumn(new JdbcTableColumState(colum.getName(), colum.getType(), colum.getJdbcType(), colum.getDefaultValue(), colum.getSize()));
            }

            for (JdbcTablePrimaryKey key : jdbcTable.getPrimaryKeys()) {
                tableState.addPrimaryKey(new JdbcPrimaryKeyState(key.getColumnName(), key.getKeySeq(), key.getPkName()));
            }
            state.addTable(tableState);
        }
        return state;
    }

    @Nonnull
    @Override
    public Class<JdbcState> getStateClass() {
        return JdbcState.class;
    }

    @Override
    public int getStateVersion() {
        return 3;
    }
}