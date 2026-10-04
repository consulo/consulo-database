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

import java.sql.ResultSetMetaData;

/**
 * A column of a query result, as the driver described it.
 *
 * @param index         the position of the column in the result, counting from 0
 * @param label         the alias when the query gives one, otherwise the column name
 * @param columnName    the name of the column in its table
 * @param jdbcType      a {@link java.sql.Types} constant
 * @param typeName      the database specific type name, for example {@code int4}, {@code timestamptz} or {@code VARCHAR}
 * @param precision     0 when unknown
 * @param scale         0 when unknown
 * @param nullable      {@link ResultSetMetaData#columnNoNulls}, {@link ResultSetMetaData#columnNullable} or
 *                      {@link ResultSetMetaData#columnNullableUnknown}
 * @param tableName     null when the column belongs to no table, for example an expression
 * @param schemaName    null when unknown
 * @param catalogName   null when unknown
 * @param autoIncrement the database fills the column itself
 * @author VISTALL
 * @since 2026-10-04
 */
@NullMarked
public record JdbcResultColumn(int index,
                               String label,
                               String columnName,
                               int jdbcType,
                               String typeName,
                               int precision,
                               int scale,
                               int nullable,
                               @Nullable String tableName,
                               @Nullable String schemaName,
                               @Nullable String catalogName,
                               boolean autoIncrement) {
}
