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
import consulo.database.datasource.jdbc.transport.DefaultJdbcDataSourceTransport;
import consulo.database.datasource.jdbc.transport.JdbcQueryResultWrapper;
import consulo.database.datasource.jdbc.transport.JdbcResultSetData;
import consulo.database.datasource.model.DataSource;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.grid.SimpleErrorInfo;
import consulo.ui.grid.GridColumn;
import consulo.ui.grid.GridLoader;
import consulo.ui.grid.GridModelUpdater;
import consulo.ui.grid.GridPagingModelImpl;
import consulo.ui.grid.GridRequestSource;
import consulo.ui.grid.GridRow;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The data source of a grid which shows one result set of an executed query, as a single page of every row. The model is filled
 * before the grid is created, so the grid loads nothing by itself. Reload runs the query again and shows the result set of the
 * same position.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@NullMarked
public final class JdbcResultGridHookUp extends JdbcGridDataHookUp {
    private final @Nullable String myQuery;
    private final int myResultSetIndex;
    private final GridPagingModelImpl.SinglePage<GridRow, GridColumn> myPageModel;
    private final JdbcResultGridLoader myLoader;

    /**
     * @param query     the statements which gave the result set, which Reload runs again; null when the result set cannot be
     *                  fetched again - Reload then explains why
     * @param resultSet the rows shown first
     * @param uiAccess  the UI thread of the grid, where the rows of a reload are put into the model
     */
    @RequiredUIAccess
    public JdbcResultGridHookUp(Project project,
                                DataSource dataSource,
                                @Nullable String query,
                                JdbcResultSetData resultSet,
                                UIAccess uiAccess) {
        super(project, dataSource, uiAccess);
        myQuery = query;
        myResultSetIndex = resultSet.index();
        myPageModel = new GridPagingModelImpl.SinglePage<>(getDataModel());
        myLoader = new JdbcResultGridLoader(this);

        JdbcGridPage page = JdbcGridPage.of(resultSet);
        GridModelUpdater modelUpdater = getModelUpdater();
        modelUpdater.setColumns(page.columns());
        modelUpdater.addRows(page.rows());
        modelUpdater.afterLastRowAdded();
    }

    @Override
    public GridPagingModelImpl.SinglePage<GridRow, GridColumn> getPageModel() {
        return myPageModel;
    }

    @Override
    public GridLoader getLoader() {
        return myLoader;
    }

    /**
     * Runs the query again, and replaces the rows with those of the result set of the same position.
     */
    @RequiredUIAccess
    void reload(GridRequestSource source) {
        String query = myQuery;
        if (query == null) {
            notifyRequestStarted(source);
            notifyRequestError(source, SimpleErrorInfo.create(LocalizeValue.localizeTODO(
                "The statements also change data or the schema, so they are not run again here. Run them from the editor.").get()));
            notifyRequestFinished(source, false);
            return;
        }

        runRequest(source, LocalizeValue.localizeTODO("Executing query..."), indicator -> fetch(indicator, query), this::onLoaded);
    }

    private JdbcGridPage fetch(ProgressIndicator indicator, String query) throws Exception {
        JdbcQueryResultWrapper result = DefaultJdbcDataSourceTransport.executeQuery(indicator, myDataSource, query);
        for (JdbcResultSetData resultSet : result.getResultSets()) {
            if (resultSet.index() == myResultSetIndex) {
                return JdbcGridPage.of(resultSet);
            }
        }

        throw new RequestFailedException(LocalizeValue.localizeTODO("The query returned " + result.getResultSetCount()
            + " result sets, there is no result set " + (myResultSetIndex + 1) + " any more").get());
    }

    @RequiredUIAccess
    private void onLoaded(GridRequestSource source, JdbcGridPage page) {
        setColumnsIfChanged(page.columns());

        GridModelUpdater modelUpdater = getModelUpdater();
        int oldRowCount = getDataModel().getRowCount();
        getDataModel().setUpdatingNow(true);
        modelUpdater.setRows(0, page.rows(), source);
        if (page.rows().size() < oldRowCount) {
            modelUpdater.removeRows(page.rows().size(), oldRowCount - page.rows().size());
        }
        modelUpdater.afterLastRowAdded();

        notifyRequestFinished(source, true);
    }
}
