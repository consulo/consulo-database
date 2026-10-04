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

import consulo.database.datasource.jdbc.transport.JdbcResultColumn;
import consulo.database.datasource.jdbc.transport.JdbcResultRow;
import consulo.database.datasource.jdbc.transport.JdbcValueDecoder;
import consulo.ui.grid.BaseObjectFormatter;
import consulo.ui.grid.DataConsumer;
import consulo.ui.grid.GridColumn;
import consulo.ui.grid.GridDataType;
import consulo.ui.grid.GridRow;
import consulo.ui.grid.GridTypeKind;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Turns the columns and rows of a JDBC result into the columns and rows of the grid.
 * <p>
 * A column gets its {@link GridTypeKind} from its {@link Types} code, refined by the type name the database gives (see
 * {@link #getKind(int, String, int)}). The values are the objects of {@link JdbcValueDecoder} - {@code null} for SQL NULL,
 * {@code java.time} values, {@link java.math.BigDecimal}, {@code byte[]} - which the grid formats as they are, except:
 * <ul>
 *     <li>an array (a {@link List}) becomes an {@code Object[]}, nested arrays too, so it is shown as {@code {a,b}}</li>
 *     <li>the text of a {@link GridTypeKind#UUID} column becomes a {@link UUID}</li>
 * </ul>
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@NullMarked
public final class JdbcGridTypes {
    /**
     * The length of the canonical text of a UUID, {@code 8-4-4-4-12} hex digits
     */
    private static final int UUID_TEXT_LENGTH = 36;

    private JdbcGridTypes() {
    }

    public static List<GridColumn> createColumns(List<JdbcResultColumn> columns) {
        List<GridColumn> result = new ArrayList<>(columns.size());
        for (JdbcResultColumn column : columns) {
            result.add(createColumn(column));
        }
        return result;
    }

    /**
     * The precision and the scale of the result are 0 when the driver does not know them. The grid prints as many digits of the
     * fraction of a second as the scale of a time column tells - so a time column of scale 0 gets an unknown scale instead, which
     * prints the digits the value has: none for a whole second, as for a column of scale 0 indeed.
     */
    public static DataConsumer.Column createColumn(JdbcResultColumn column) {
        String name = column.label().isEmpty() ? column.columnName() : column.label();
        GridTypeKind kind = getKind(column.jdbcType(), column.typeName(), column.precision());
        int precision = column.precision() > 0 ? column.precision() : -1;
        int scale = isTemporal(kind) && column.scale() <= 0 ? BaseObjectFormatter.UNKNOWN_SCALE : column.scale();
        return new DataConsumer.Column(column.index(), name, GridDataType.of(kind, column.typeName()), precision, scale);
    }

    private static boolean isTemporal(GridTypeKind kind) {
        return kind == GridTypeKind.TIME || kind == GridTypeKind.TIMESTAMP || kind == GridTypeKind.TIMESTAMP_TZ;
    }

    /**
     * @param columns the columns of the grid, made by {@link #createColumns} from the columns of the rows
     */
    public static List<GridRow> createRows(List<JdbcResultRow> rows, List<? extends GridColumn> columns) {
        List<GridRow> result = new ArrayList<>(rows.size());
        for (JdbcResultRow row : rows) {
            result.add(createRow(row, columns));
        }
        return result;
    }

    /**
     * @param columns the columns of the grid, made by {@link #createColumns} from the columns of the row
     */
    public static GridRow createRow(JdbcResultRow row, List<? extends GridColumn> columns) {
        List<@Nullable Object> values = row.getValues();
        @Nullable Object[] cells = new Object[values.size()];
        for (int i = 0; i < cells.length; i++) {
            GridTypeKind kind = i < columns.size() ? columns.get(i).getType().getKind() : GridTypeKind.OTHER;
            cells[i] = toGridValue(values.get(i), kind);
        }
        // the number of the row in the grid counts from 1, the index of the row from 0
        int index = (int) Math.min(row.getIndex(), Integer.MAX_VALUE - 1);
        return DataConsumer.Row.create(index, cells);
    }

    /**
     * @param value a value decoded by {@link JdbcValueDecoder}
     * @param kind  the kind of the column of the value
     */
    public static @Nullable Object toGridValue(@Nullable Object value, GridTypeKind kind) {
        if (value instanceof List<?> list) {
            return toArray(list);
        }
        if (kind == GridTypeKind.UUID && value instanceof String text) {
            return toUUID(text);
        }
        return value;
    }

    private static @Nullable Object[] toArray(List<?> list) {
        @Nullable Object[] array = new Object[list.size()];
        for (int i = 0; i < array.length; i++) {
            @Nullable Object item = list.get(i);
            array[i] = item instanceof List<?> nested ? toArray(nested) : item;
        }
        return array;
    }

    private static Object toUUID(String text) {
        if (text.length() != UUID_TEXT_LENGTH) {
            return text;
        }
        try {
            return UUID.fromString(text);
        }
        catch (IllegalArgumentException e) {
            return text;
        }
    }

    /**
     * The kind of a column by its {@link Types} code, refined first by its type name, case insensitively:
     * <ul>
     *     <li>{@code uuid} - {@link GridTypeKind#UUID}</li>
     *     <li>{@code json}, {@code jsonb} - {@link GridTypeKind#JSON}</li>
     *     <li>{@code xml} - {@link GridTypeKind#XML}</li>
     *     <li>{@code interval...} - {@link GridTypeKind#INTERVAL}</li>
     *     <li>{@code bytea} - {@link GridTypeKind#BINARY}</li>
     *     <li>{@code timestamptz}, {@code timestamp... with time zone} - {@link GridTypeKind#TIMESTAMP_TZ}</li>
     *     <li>{@code timetz}, {@code time... with time zone} - {@link GridTypeKind#TIME}, there is no kind of a time with a zone</li>
     *     <li>a name starting with {@code _} of an {@link Types#ARRAY} or {@link Types#OTHER} column - {@link GridTypeKind#ARRAY},
     *     as PostgreSQL names its array types ({@code _int4})</li>
     * </ul>
     *
     * @param precision a {@link Types#BIT} column of a precision up to 1 is {@link GridTypeKind#BOOLEAN}, a wider one
     *                  {@link GridTypeKind#BINARY}
     */
    public static GridTypeKind getKind(int jdbcType, String typeName, int precision) {
        GridTypeKind kindByName = getKindByName(jdbcType, typeName.trim().toLowerCase(Locale.ROOT));
        if (kindByName != null) {
            return kindByName;
        }

        return switch (jdbcType) {
            case Types.BIT -> precision <= 1 ? GridTypeKind.BOOLEAN : GridTypeKind.BINARY;
            case Types.BOOLEAN -> GridTypeKind.BOOLEAN;
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT -> GridTypeKind.INTEGER;
            case Types.REAL, Types.FLOAT, Types.DOUBLE -> GridTypeKind.FLOAT;
            case Types.NUMERIC, Types.DECIMAL -> GridTypeKind.DECIMAL;
            case Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR, Types.NCHAR, Types.NVARCHAR, Types.LONGNVARCHAR, Types.CLOB, Types.NCLOB ->
                GridTypeKind.TEXT;
            case Types.DATE -> GridTypeKind.DATE;
            case Types.TIME, Types.TIME_WITH_TIMEZONE -> GridTypeKind.TIME;
            case Types.TIMESTAMP -> GridTypeKind.TIMESTAMP;
            case Types.TIMESTAMP_WITH_TIMEZONE -> GridTypeKind.TIMESTAMP_TZ;
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB -> GridTypeKind.BINARY;
            case Types.ARRAY -> GridTypeKind.ARRAY;
            case Types.SQLXML -> GridTypeKind.XML;
            default -> GridTypeKind.OTHER;
        };
    }

    /**
     * @param name the type name in lower case
     */
    private static @Nullable GridTypeKind getKindByName(int jdbcType, String name) {
        switch (name) {
            case "uuid":
                return GridTypeKind.UUID;
            case "json":
            case "jsonb":
                return GridTypeKind.JSON;
            case "xml":
                return GridTypeKind.XML;
            case "bytea":
                return GridTypeKind.BINARY;
            case "timestamptz":
                return GridTypeKind.TIMESTAMP_TZ;
            case "timetz":
                return GridTypeKind.TIME;
            default:
                break;
        }

        if (name.startsWith("interval")) {
            return GridTypeKind.INTERVAL;
        }

        if (name.endsWith(" with time zone")) {
            if (name.startsWith("timestamp")) {
                return GridTypeKind.TIMESTAMP_TZ;
            }
            if (name.startsWith("time")) {
                return GridTypeKind.TIME;
            }
        }

        if (name.startsWith("_") && (jdbcType == Types.ARRAY || jdbcType == Types.OTHER)) {
            return GridTypeKind.ARRAY;
        }
        return null;
    }
}
