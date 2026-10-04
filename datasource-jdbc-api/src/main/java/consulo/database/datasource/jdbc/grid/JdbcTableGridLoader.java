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

import consulo.localize.LocalizeValue;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.grid.GridLoaderBase;
import consulo.ui.grid.GridColumn;
import consulo.ui.grid.GridModelUpdater;
import consulo.ui.grid.GridRequestSource;
import consulo.ui.grid.GridRow;
import consulo.ui.grid.MultiPageModel;
import org.jspecify.annotations.NullMarked;

/**
 * Loads a page of a table in a background task, and puts it into the model on the UI thread. The count of all rows comes with
 * every page, so the total is always precise and never needs an update of its own.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@NullMarked
final class JdbcTableGridLoader extends GridLoaderBase {
    private final JdbcTableGridHookUp myTableHookUp;

    JdbcTableGridLoader(JdbcTableGridHookUp hookUp, MultiPageModel<GridRow, GridColumn> pageModel, GridModelUpdater modelUpdater) {
        super(hookUp, pageModel, modelUpdater);
        myTableHookUp = hookUp;
    }

    /**
     * @param offset the index of the first row, counting from 0; less than 0 counts from the end (the last page)
     */
    @Override
    @RequiredUIAccess
    public void load(GridRequestSource source, int offset) {
        int pageSize = getPageModel().getPageSize();
        myTableHookUp.runRequest(source,
            LocalizeValue.localizeTODO("Fetching data..."),
            indicator -> myTableHookUp.fetchPage(indicator, offset, pageSize),
            this::onLoaded);
    }

    @RequiredUIAccess
    private void onLoaded(GridRequestSource source, JdbcGridPage page) {
        myTableHookUp.setColumnsIfChanged(page.columns());

        MultiPageModel<GridRow, GridColumn> pageModel = getPageModel();
        pageModel.setTotalRowCount(page.totalRowCount(), true);
        pageModel.setTotalRowCountUpdateable(false);

        loadingStarted(page.offset());
        afterLastRowAdded(addRows(page.rows(), 0, source), source);
        myTableHookUp.notifyRequestFinished(source, true);
    }
}
