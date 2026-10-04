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

package consulo.database.datasource.jdbc.transport;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A row of a query result with its values decoded by {@link JdbcValueDecoder}. Rows compare by identity.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@NullMarked
public final class JdbcResultRow {
    private final long myIndex;
    private final List<@Nullable Object> myValues;

    /**
     * @param values unmodifiable, one per column; null is SQL NULL
     */
    public JdbcResultRow(long index, List<@Nullable Object> values) {
        myIndex = index;
        myValues = values;
    }

    /**
     * @return the position of the row in the whole result, counting from 0 (skipped rows included)
     */
    public long getIndex() {
        return myIndex;
    }

    /**
     * @return unmodifiable, one value per column
     */
    public List<@Nullable Object> getValues() {
        return myValues;
    }

    /**
     * @return the value of the column, null for SQL NULL
     */
    public @Nullable Object getValue(int columnIndex) {
        return myValues.get(columnIndex);
    }
}
