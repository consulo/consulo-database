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

import consulo.database.jdbc.rt.shared.JdbcColumnMeta;
import consulo.database.jdbc.rt.shared.JdbcQueryRow;
import consulo.database.jdbc.rt.shared.JdbcValue;
import consulo.database.jdbc.rt.shared.JdbcValueType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * The only place where the protocol objects of the JDBC runtime become plain Java objects.
 * <p>
 * Value kinds and the objects they decode to:
 * <ul>
 *     <li>{@code _null}, or a kind whose value field is unset - {@code null}</li>
 *     <li>{@code _int} - {@link Integer}, {@code _long} - {@link Long}, {@code _double} - {@link Double},
 *     {@code _decimal} - {@link BigDecimal}, {@code _bool} - {@link Boolean}</li>
 *     <li>{@code _string}, {@code _other} - {@link String}</li>
 *     <li>{@code _bytes} - {@code byte[]}</li>
 *     <li>{@code _date} - {@link LocalDate}, {@code _time} - {@link LocalTime} or {@link OffsetTime},
 *     {@code _timestamp} - {@link LocalDateTime}, {@code _timestamptz} - {@link OffsetDateTime}</li>
 *     <li>{@code _array} - an unmodifiable {@link List} of decoded elements, which may contain null</li>
 * </ul>
 * A text which does not parse as its kind is returned as the {@link String} itself.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@NullMarked
public final class JdbcValueDecoder {
    private JdbcValueDecoder() {
    }

    public static @Nullable Object decode(JdbcValue value) {
        JdbcValueType type = value.getType();
        if (type == null) {
            return null;
        }

        return switch (type) {
            case _null -> null;
            case _int -> value.isSetIntValue() ? Integer.valueOf(value.getIntValue()) : null;
            case _long -> value.isSetLongValue() ? Long.valueOf(value.getLongValue()) : null;
            case _double -> value.isSetDoubleValue() ? Double.valueOf(value.getDoubleValue()) : null;
            case _bool -> value.isSetBoolValue() ? Boolean.valueOf(value.isBoolValue()) : null;
            case _string, _other -> value.isSetStringValue() ? value.getStringValue() : null;
            case _decimal -> decodeText(value, BigDecimal::new);
            case _date -> decodeText(value, LocalDate::parse);
            case _time -> decodeText(value, JdbcValueDecoder::parseTime);
            case _timestamp -> decodeText(value, LocalDateTime::parse);
            case _timestamptz -> decodeText(value, OffsetDateTime::parse);
            case _bytes -> value.isSetBytesValue() ? value.getBytesValue() : null;
            case _array -> value.isSetArrayValue() ? decodeList(value.getArrayValue()) : null;
        };
    }

    public static JdbcResultRow decodeRow(JdbcQueryRow row) {
        List<JdbcValue> values = row.isSetValues() ? row.getValues() : List.of();
        return new JdbcResultRow(row.getIndex(), decodeList(values));
    }

    public static JdbcResultColumn decodeColumn(int index, JdbcColumnMeta column) {
        return new JdbcResultColumn(index,
            emptyIfNull(column.getLabel()),
            emptyIfNull(column.getColumnName()),
            column.getJdbcType(),
            emptyIfNull(column.getTypeName()),
            column.getPrecision(),
            column.getScale(),
            column.getNullable(),
            column.isSetTableName() ? column.getTableName() : null,
            column.isSetSchemaName() ? column.getSchemaName() : null,
            column.isSetCatalogName() ? column.getCatalogName() : null,
            column.isAutoIncrement());
    }

    private static List<@Nullable Object> decodeList(List<JdbcValue> values) {
        // List.copyOf() and List.of() reject null elements, which are SQL NULL here
        List<@Nullable Object> result = new ArrayList<>(values.size());
        for (JdbcValue value : values) {
            result.add(decode(value));
        }
        return Collections.unmodifiableList(result);
    }

    private static @Nullable Object decodeText(JdbcValue value, Function<String, Object> parser) {
        if (!value.isSetStringValue()) {
            return null;
        }

        String text = value.getStringValue();
        try {
            return parser.apply(text);
        }
        catch (RuntimeException e) {
            return text;
        }
    }

    private static Object parseTime(String text) {
        // a local time has neither a sign nor a zone letter
        if (text.indexOf('+') >= 0 || text.indexOf('-') >= 0 || text.endsWith("Z")) {
            return OffsetTime.parse(text);
        }
        return LocalTime.parse(text);
    }

    private static String emptyIfNull(@Nullable String text) {
        return text == null ? "" : text;
    }
}
