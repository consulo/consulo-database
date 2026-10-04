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

import consulo.database.datasource.jdbc.transport.JdbcResultSetData;
import consulo.ui.grid.GridColumn;
import consulo.ui.grid.GridRow;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * Rows loaded for the grid, made on a background thread and put into the model on the UI thread.
 *
 * @param offset        the index of the first row in the source, counting from 0
 * @param totalRowCount the count of all rows of the source
 * @author VISTALL
 * @since 2026-10-04
 */
@NullMarked
record JdbcGridPage(List<GridColumn> columns, List<GridRow> rows, int offset, long totalRowCount) {
    /**
     * A page holding every row of a result set
     */
    static JdbcGridPage of(JdbcResultSetData resultSet) {
        List<GridColumn> columns = JdbcGridTypes.createColumns(resultSet.columns());
        List<GridRow> rows = JdbcGridTypes.createRows(resultSet.rows(), columns);
        return new JdbcGridPage(columns, rows, 0, rows.size());
    }
}
