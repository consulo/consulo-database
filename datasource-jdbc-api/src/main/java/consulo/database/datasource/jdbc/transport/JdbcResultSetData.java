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

import java.util.List;

/**
 * One decoded result set of an execution.
 *
 * @param index   the position of the result set among the result sets of the execution, counting from 0 - the update counts
 *                between them are not counted
 * @param columns unmodifiable
 * @param rows    unmodifiable
 * @param hasMore the result set has more rows than were returned
 * @author VISTALL
 * @since 2026-10-04
 */
@NullMarked
public record JdbcResultSetData(int index, List<JdbcResultColumn> columns, List<JdbcResultRow> rows, boolean hasMore) {
}
