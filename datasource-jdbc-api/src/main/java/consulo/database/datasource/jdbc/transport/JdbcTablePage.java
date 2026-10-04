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

/**
 * A page of the rows of a table.
 *
 * @param result the rows of the page; {@link JdbcQueryResultWrapper#getAllRowsCount()} is the count of all rows of the table
 * @param offset the index of the first row of the page in the table, counting from 0
 * @author VISTALL
 * @since 2026-10-04
 */
@NullMarked
public record JdbcTablePage(JdbcQueryResultWrapper result, int offset) {
}
