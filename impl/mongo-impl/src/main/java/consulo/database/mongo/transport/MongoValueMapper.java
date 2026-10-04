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


package consulo.database.mongo.transport;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps {@link MongoValue}s - the BSON values the runtime process sends - to the Java values a result shows. A missing field stays
 * apart from a field holding {@code null}, dates are {@link Instant}s and UUIDs are decoded.
 *
 * <table>
 * <tr><th>BSON</th><th>Java</th></tr>
 * <tr><td>missing field</td><td>{@link MongoValues#MISSING}</td></tr>
 * <tr><td>null, undefined</td><td>{@code null}</td></tr>
 * <tr><td>string, int, long, double, bool</td><td>{@code String}, {@code Integer}, {@code Long}, {@code Double}, {@code Boolean}</td></tr>
 * <tr><td>decimal</td><td>{@code BigDecimal}; NaN, infinities and negative zero are {@link MongoDecimalValue}</td></tr>
 * <tr><td>date</td><td>{@link Instant}</td></tr>
 * <tr><td>objectId</td><td>{@link MongoObjectId}</td></tr>
 * <tr><td>binData subtype 4 of 16 bytes</td><td>{@code java.util.UUID}</td></tr>
 * <tr><td>other binData</td><td>{@link MongoBinaryValue}</td></tr>
 * <tr><td>object</td><td>{@code LinkedHashMap<String, Object>}, mapped recursively</td></tr>
 * <tr><td>array</td><td>{@code ArrayList<Object>}, mapped recursively</td></tr>
 * <tr><td>timestamp</td><td>{@link MongoTimestampValue}</td></tr>
 * <tr><td>regex, javascript, symbol, dbPointer, minKey, maxKey</td><td>{@code String}: {@code /pattern/options}, the code, the
 * symbol, {@code DBPointer("ns", id)}, {@code MinKey}, {@code MaxKey}</td></tr>
 * </table>
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public final class MongoValueMapper {
    // the BSON type aliases of the MongoDB $type operator, plus "uuid" for a UUID binary
    public static final String TYPE_DOUBLE = "double";
    public static final String TYPE_STRING = "string";
    public static final String TYPE_OBJECT = "object";
    public static final String TYPE_ARRAY = "array";
    public static final String TYPE_BINARY = "binData";
    public static final String TYPE_UUID = "uuid";
    public static final String TYPE_UNDEFINED = "undefined";
    public static final String TYPE_OBJECT_ID = "objectId";
    public static final String TYPE_BOOL = "bool";
    public static final String TYPE_DATE = "date";
    public static final String TYPE_NULL = "null";
    public static final String TYPE_REGEX = "regex";
    public static final String TYPE_DB_POINTER = "dbPointer";
    public static final String TYPE_JAVASCRIPT = "javascript";
    public static final String TYPE_SYMBOL = "symbol";
    public static final String TYPE_JAVASCRIPT_WITH_SCOPE = "javascriptWithScope";
    public static final String TYPE_INT = "int";
    public static final String TYPE_TIMESTAMP = "timestamp";
    public static final String TYPE_LONG = "long";
    public static final String TYPE_DECIMAL = "decimal";
    public static final String TYPE_MIN_KEY = "minKey";
    public static final String TYPE_MAX_KEY = "maxKey";

    private MongoValueMapper() {
    }

    /**
     * @param value the value of a top level field, {@code null} when the document has no such field
     * @return the cell value, {@link MongoValues#MISSING} for a missing field
     */
    public static @Nullable Object toCellValue(@Nullable MongoValue value) {
        if (value == null) {
            return MongoValues.MISSING;
        }
        return toJavaValue(value);
    }

    /**
     * @param columns the top level field names of the result
     * @return the cell values of the document, positional by {@code columns}
     */
    public static @Nullable Object[] toRow(MongoValue document, List<String> columns) {
        List<MongoField> fields = document.getFields();
        Map<String, MongoValue> values = new HashMap<>(fields.size() * 2);
        for (MongoField field : fields) {
            // the first of duplicate names wins, as in MongoValue.get()
            values.putIfAbsent(field.name(), field.value());
        }

        @Nullable Object[] row = new @Nullable Object[columns.size()];
        for (int i = 0; i < row.length; i++) {
            row[i] = toCellValue(values.get(columns.get(i)));
        }
        return row;
    }

    public static @Nullable Object toJavaValue(MongoValue value) {
        return switch (value.getKind()) {
            case NULL, UNDEFINED -> null;
            case DOCUMENT -> toMap(value);
            case ARRAY -> toList(value);
            // the scalar of OTHER is its display text
            default -> value.getScalar();
        };
    }

    /**
     * @return the BSON type alias of the value as the MongoDB {@code $type} operator names it, and {@code uuid} for a UUID binary
     */
    public static String getTypeName(MongoValue value) {
        return value.getTypeName();
    }

    public static boolean isNull(MongoValue value) {
        return value.isNull();
    }

    private static Map<String, @Nullable Object> toMap(MongoValue document) {
        Map<String, @Nullable Object> map = new LinkedHashMap<>();
        for (MongoField field : document.getFields()) {
            if (!map.containsKey(field.name())) {
                map.put(field.name(), toJavaValue(field.value()));
            }
        }
        return map;
    }

    private static List<@Nullable Object> toList(MongoValue array) {
        List<MongoValue> items = array.getItems();
        List<@Nullable Object> list = new ArrayList<>(items.size());
        for (MongoValue item : items) {
            list.add(toJavaValue(item));
        }
        return list;
    }
}
