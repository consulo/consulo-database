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
import consulo.database.datasource.jdbc.provider.impl.JdbcTableState;
import consulo.database.datasource.jdbc.transport.DefaultJdbcDataSourceTransport;
import consulo.database.datasource.jdbc.transport.JdbcQueryResultWrapper;
import consulo.database.datasource.jdbc.transport.JdbcTablePage;
import consulo.database.datasource.model.DataSource;
import consulo.project.Project;
import consulo.ui.UIAccess;
import consulo.ui.ex.grid.MultiPageModelImpl;
import consulo.ui.grid.GridColumn;
import consulo.ui.grid.GridLoader;
import consulo.ui.grid.GridRow;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * The data source of a grid which pages through the rows of a table. Every page is fetched again: the count of all rows, then
 * the rows of the page, ordered by the primary key when it is known, in one session of the transport.
 * <p>
 * The grid loads the first page by itself once it is shown.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@NullMarked
public final class JdbcTableGridHookUp extends JdbcGridDataHookUp {
    private final String myDatabaseName;
    private final String myChildId;
    private final MultiPageModelImpl<GridRow, GridColumn> myPageModel;
    private final JdbcTableGridLoader myLoader;

    /**
     * @param childId  the table, {@link JdbcTableState#getNameWithScheme()}
     * @param uiAccess the UI thread of the grid, where the pages are put into the model
     */
    public JdbcTableGridHookUp(Project project, DataSource dataSource, String databaseName, String childId, UIAccess uiAccess) {
        super(project, dataSource, uiAccess);
        myDatabaseName = databaseName;
        myChildId = childId;
        // there are no grid settings yet - a page holds DataGridSettings.DEFAULT_PAGE_SIZE rows, until the user picks another size
        myPageModel = new MultiPageModelImpl<>(getDataModel(), null);
        myLoader = new JdbcTableGridLoader(this, myPageModel, getModelUpdater());
    }

    @Override
    public MultiPageModelImpl<GridRow, GridColumn> getPageModel() {
        return myPageModel;
    }

    @Override
    public GridLoader getLoader() {
        return myLoader;
    }

    /**
     * Fetches a page, on a background thread.
     *
     * @param offset   the index of the first row, counting from 0; less than 0 counts from the end
     * @param pageSize the maximum count of rows, less than 1 for all rows
     */
    JdbcGridPage fetchPage(ProgressIndicator indicator, int offset, int pageSize) throws Exception {
        JdbcTablePage page =
            DefaultJdbcDataSourceTransport.fetchTablePage(indicator, myProject, myDataSource, myDatabaseName, myChildId, offset, pageSize);

        JdbcQueryResultWrapper result = page.result();
        List<GridColumn> columns = JdbcGridTypes.createColumns(result.getColumns());
        List<GridRow> rows = JdbcGridTypes.createRows(result.getRows(), columns);
        return new JdbcGridPage(columns, rows, page.offset(), result.getAllRowsCount());
    }
}
