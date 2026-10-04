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

import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.grid.GridLoader;
import consulo.ui.grid.GridRequestSource;
import org.jspecify.annotations.NullMarked;

/**
 * The loader of a single page of every row of a result set. Only Reload runs the query again: there is nothing to page through,
 * and a page size changes nothing, so the other requests - which could repeat the changes a script makes - end at once.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@NullMarked
final class JdbcResultGridLoader implements GridLoader {
    private final JdbcResultGridHookUp myHookUp;

    JdbcResultGridLoader(JdbcResultGridHookUp hookUp) {
        myHookUp = hookUp;
    }

    @Override
    @RequiredUIAccess
    public void reloadCurrentPage(GridRequestSource source) {
        myHookUp.reload(source);
    }

    @Override
    @RequiredUIAccess
    public void loadNextPage(GridRequestSource source) {
        myHookUp.finishUnchanged(source);
    }

    @Override
    @RequiredUIAccess
    public void loadPreviousPage(GridRequestSource source) {
        myHookUp.finishUnchanged(source);
    }

    @Override
    @RequiredUIAccess
    public void loadLastPage(GridRequestSource source) {
        myHookUp.finishUnchanged(source);
    }

    @Override
    @RequiredUIAccess
    public void loadFirstPage(GridRequestSource source) {
        myHookUp.finishUnchanged(source);
    }

    @Override
    @RequiredUIAccess
    public void load(GridRequestSource source, int offset) {
        myHookUp.finishUnchanged(source);
    }

    @Override
    @RequiredUIAccess
    public void updateTotalRowCount(GridRequestSource source) {
        myHookUp.notifyRequestFinished(source, false);
    }

    @Override
    @RequiredUIAccess
    public void applyFilterAndSorting(GridRequestSource source) {
        myHookUp.notifyRequestFinished(source, false);
    }

    @Override
    public void updateIsTotalRowCountUpdateable() {
    }
}
