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

package consulo.database.datasource.jdbc.transport.ui;

import consulo.annotation.component.ExtensionImpl;
import consulo.database.datasource.jdbc.grid.JdbcGridDataHookUp;
import consulo.database.datasource.jdbc.grid.JdbcResultGridHookUp;
import consulo.database.datasource.jdbc.grid.JdbcTableGridHookUp;
import consulo.database.datasource.jdbc.provider.JdbcDataSourceProvider;
import consulo.database.datasource.jdbc.transport.JdbcQueryResultWrapper;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.transport.ui.DataSourceTransportResultPresentation;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.Space;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.grid.DataGrid;
import consulo.ui.layout.DockLayout;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Shows JDBC rows in the data grid, read-only: a table page by page, and a result set of a query as one page. A query which
 * returned no result set shows how many rows it changed.
 *
 * @author VISTALL
 * @since 21/10/2021
 */
@NullMarked
@ExtensionImpl(order = "last")
public class DefaultJdbcDataSourceTransportResultPresentation implements DataSourceTransportResultPresentation<JdbcQueryResultWrapper> {
    @Override
    public boolean accept(DataSource dataSource) {
        return dataSource.getProvider() instanceof JdbcDataSourceProvider;
    }

    /**
     * @return one part per result set; the whole result when there is none - it shows the update counts then
     */
    @Override
    public List<JdbcQueryResultWrapper> splitResult(JdbcQueryResultWrapper result) {
        return result.splitByResultSet();
    }

    /**
     * @return the grid of the first result set, which runs the query again on Reload; the update counts when there is no result set
     */
    @RequiredUIAccess
    @Override
    public Component buildComponentForResult(JdbcQueryResultWrapper result,
                                             Project project,
                                             DataSource dataSource,
                                             @Nullable String dbName,
                                             @Nullable String childId,
                                             Disposable parent) {
        if (!result.hasResultSet()) {
            return buildUpdateCountView(result.getUpdateCounts());
        }

        JdbcResultGridHookUp hookUp =
            new JdbcResultGridHookUp(project, dataSource, result.getQuery(), result.getResultSets().get(0), UIAccess.current());
        return createGrid(hookUp, parent);
    }

    @RequiredUIAccess
    @Override
    public Component buildComponentForChild(Project project, DataSource dataSource, String dbName, String childId, Disposable parent) {
        return createGrid(new JdbcTableGridHookUp(project, dataSource, dbName, childId, UIAccess.current()), parent);
    }

    @RequiredUIAccess
    private static DataGrid createGrid(JdbcGridDataHookUp hookUp, Disposable parent) {
        // registered first, so it is disposed after the grid
        Disposer.register(parent, hookUp);

        // read-only: without cell editors the grid edits nothing
        DataGrid grid = DataGrid.create(hookUp, (dataGrid, appearance) -> appearance.setResultViewShowRowNumbers(true));
        Disposer.register(parent, grid);
        return grid;
    }

    @RequiredUIAccess
    private static Component buildUpdateCountView(List<Long> updateCounts) {
        Label label = Label.create(buildUpdateCountMessage(updateCounts));
        label.paddingBuilder().allSet(Space.MEDIUM).apply();

        // the message stays in the top left corner, however big the view is
        DockLayout layout = DockLayout.create();
        layout.top(label);
        return layout;
    }

    /**
     * @param updateCounts the counts of the statements which returned no rows, empty when there were none (for example DDL
     *                     on a driver which reports no count)
     */
    private static LocalizeValue buildUpdateCountMessage(List<Long> updateCounts) {
        if (updateCounts.isEmpty()) {
            return LocalizeValue.localizeTODO("Statement executed");
        }

        long total = 0;
        for (Long updateCount : updateCounts) {
            total += Math.max(updateCount, 0);
        }

        String rows = total == 1 ? "1 row affected" : total + " rows affected";
        if (updateCounts.size() == 1) {
            return LocalizeValue.localizeTODO(rows);
        }
        return LocalizeValue.localizeTODO(rows + " by " + updateCounts.size() + " statements");
    }
}
