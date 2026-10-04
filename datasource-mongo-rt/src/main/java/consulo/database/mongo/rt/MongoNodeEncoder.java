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

package consulo.database.mongo.rt;

import consulo.database.mongo.rt.shared.MongoFieldShape;
import consulo.database.mongo.rt.shared.MongoNode;
import consulo.database.mongo.rt.shared.MongoNodeKind;
import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.BsonDbPointer;
import org.bson.BsonDocument;
import org.bson.BsonJavaScriptWithScope;
import org.bson.BsonRegularExpression;
import org.bson.BsonValue;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Encodes BSON values into {@link MongoNode} trees without losing their type: a document keeps its field order, an int stays
 * an int and a long a long, decimals travel as text, binary data keeps its subtype.
 * <p>
 * Types without a node kind of their own are {@link MongoNodeKind#OTHER} with the {@code $type} alias as type name:
 * <table>
 * <tr><th>BSON</th><th>text</th><th>also</th></tr>
 * <tr><td>regex</td><td>the pattern</td><td>options: the flags</td></tr>
 * <tr><td>javascript</td><td>the code</td><td></td></tr>
 * <tr><td>javascriptWithScope</td><td>the code</td><td>children: the fields of the scope</td></tr>
 * <tr><td>symbol</td><td>the symbol</td><td></td></tr>
 * <tr><td>dbPointer</td><td>{@code DBPointer("namespace", hex)}</td><td></td></tr>
 * <tr><td>minKey, maxKey</td><td>{@code MinKey}, {@code MaxKey}</td><td></td></tr>
 * <tr><td>a type of a newer driver</td><td>its display text</td><td>the lower case type name</td></tr>
 * </table>
 *
 * @author VISTALL
 * @since 2026-10-04
 */
final class MongoNodeEncoder {
    // the BSON type aliases of the MongoDB $type operator
    static final String TYPE_REGEX = "regex";
    static final String TYPE_JAVASCRIPT = "javascript";
    static final String TYPE_JAVASCRIPT_WITH_SCOPE = "javascriptWithScope";
    static final String TYPE_SYMBOL = "symbol";
    static final String TYPE_DB_POINTER = "dbPointer";
    static final String TYPE_MIN_KEY = "minKey";
    static final String TYPE_MAX_KEY = "maxKey";

    private static final int UUID_LENGTH = 16;

    private MongoNodeEncoder() {
    }

    /**
     * @param name the field name when the parent is a document, {@code null} for an array item and for a root
     */
    static MongoNode encode(@Nullable String name, BsonValue value) {
        MongoNode node;
        switch (value.getBsonType()) {
            case NULL -> node = new MongoNode(MongoNodeKind.NULL);
            case UNDEFINED -> node = new MongoNode(MongoNodeKind.UNDEFINED);
            case BOOLEAN -> node = new MongoNode(MongoNodeKind.BOOL).setBoolValue(value.asBoolean().getValue());
            case INT32 -> node = new MongoNode(MongoNodeKind.INT32).setIntValue(value.asInt32().getValue());
            case INT64 -> node = new MongoNode(MongoNodeKind.INT64).setLongValue(value.asInt64().getValue());
            case DOUBLE -> node = new MongoNode(MongoNodeKind.DOUBLE).setDoubleValue(value.asDouble().getValue());
            // Decimal128.toString() is exact, and also covers NaN, the infinities and negative zero
            case DECIMAL128 -> node = new MongoNode(MongoNodeKind.DECIMAL128).setText(value.asDecimal128().getValue().toString());
            case STRING -> node = new MongoNode(MongoNodeKind.STRING).setText(value.asString().getValue());
            case DATE_TIME -> node = new MongoNode(MongoNodeKind.DATE).setLongValue(value.asDateTime().getValue());
            case TIMESTAMP -> node = new MongoNode(MongoNodeKind.TIMESTAMP).setLongValue(value.asTimestamp().getValue());
            case OBJECT_ID -> node = new MongoNode(MongoNodeKind.OBJECT_ID).setText(value.asObjectId().getValue().toHexString());
            case BINARY -> node = encodeBinary(value.asBinary());
            case DOCUMENT -> node = new MongoNode(MongoNodeKind.DOCUMENT).setChildren(encodeFields(value.asDocument()));
            case ARRAY -> node = new MongoNode(MongoNodeKind.ARRAY).setChildren(encodeItems(value.asArray()));
            default -> node = encodeOther(value);
        }

        if (name != null) {
            node.setName(name);
        }
        return node;
    }

    /**
     * @return the shape of a top level field: its name and kind, without the value
     */
    static MongoFieldShape shape(String name, BsonValue value) {
        MongoFieldShape shape = new MongoFieldShape(name, kindOf(value));
        @Nullable String typeName = otherTypeName(value);
        if (typeName != null) {
            shape.setTypeName(typeName);
        }
        return shape;
    }

    static MongoNodeKind kindOf(BsonValue value) {
        return switch (value.getBsonType()) {
            case NULL -> MongoNodeKind.NULL;
            case UNDEFINED -> MongoNodeKind.UNDEFINED;
            case BOOLEAN -> MongoNodeKind.BOOL;
            case INT32 -> MongoNodeKind.INT32;
            case INT64 -> MongoNodeKind.INT64;
            case DOUBLE -> MongoNodeKind.DOUBLE;
            case DECIMAL128 -> MongoNodeKind.DECIMAL128;
            case STRING -> MongoNodeKind.STRING;
            case DATE_TIME -> MongoNodeKind.DATE;
            case TIMESTAMP -> MongoNodeKind.TIMESTAMP;
            case OBJECT_ID -> MongoNodeKind.OBJECT_ID;
            case BINARY -> toUuid(value.asBinary()) != null ? MongoNodeKind.UUID : MongoNodeKind.BINARY;
            case DOCUMENT -> MongoNodeKind.DOCUMENT;
            case ARRAY -> MongoNodeKind.ARRAY;
            default -> MongoNodeKind.OTHER;
        };
    }

    /**
     * @return the {@code $type} alias of a value of kind {@link MongoNodeKind#OTHER}, {@code null} for any other kind
     */
    static @Nullable String otherTypeName(BsonValue value) {
        return switch (value.getBsonType()) {
            case NULL, UNDEFINED, BOOLEAN, INT32, INT64, DOUBLE, DECIMAL128, STRING, DATE_TIME, TIMESTAMP, OBJECT_ID, BINARY, DOCUMENT,
                 ARRAY -> null;
            case REGULAR_EXPRESSION -> TYPE_REGEX;
            case JAVASCRIPT -> TYPE_JAVASCRIPT;
            case JAVASCRIPT_WITH_SCOPE -> TYPE_JAVASCRIPT_WITH_SCOPE;
            case SYMBOL -> TYPE_SYMBOL;
            case DB_POINTER -> TYPE_DB_POINTER;
            case MIN_KEY -> TYPE_MIN_KEY;
            case MAX_KEY -> TYPE_MAX_KEY;
            default -> value.getBsonType().name().toLowerCase(Locale.ROOT);
        };
    }

    private static List<MongoNode> encodeFields(BsonDocument document) {
        List<MongoNode> children = new ArrayList<>(document.size());
        for (Map.Entry<String, BsonValue> entry : document.entrySet()) {
            children.add(encode(entry.getKey(), entry.getValue()));
        }
        return children;
    }

    private static List<MongoNode> encodeItems(BsonArray array) {
        List<MongoNode> children = new ArrayList<>(array.size());
        for (BsonValue item : array) {
            children.add(encode(null, item));
        }
        return children;
    }

    private static MongoNode encodeBinary(BsonBinary binary) {
        @Nullable UUID uuid = toUuid(binary);
        if (uuid != null) {
            return new MongoNode(MongoNodeKind.UUID).setText(uuid.toString());
        }

        // subtype 3 (legacy UUID) stays binary: its byte order depends on the driver which wrote it
        return new MongoNode(MongoNodeKind.BINARY).setBinarySubtype(binary.getType()).setBytes(binary.getData());
    }

    private static @Nullable UUID toUuid(BsonBinary binary) {
        if (binary.getType() != BsonBinarySubType.UUID_STANDARD.getValue() || binary.getData().length != UUID_LENGTH) {
            return null;
        }

        try {
            return binary.asUuid();
        }
        catch (RuntimeException e) {
            return null;
        }
    }

    private static MongoNode encodeOther(BsonValue value) {
        @Nullable String typeName = otherTypeName(value);
        MongoNode node = new MongoNode(MongoNodeKind.OTHER).setTypeName(typeName == null ? "unknown" : typeName);
        switch (value.getBsonType()) {
            case REGULAR_EXPRESSION -> {
                BsonRegularExpression regex = value.asRegularExpression();
                node.setText(regex.getPattern());
                node.setOptions(regex.getOptions());
            }
            case JAVASCRIPT -> node.setText(value.asJavaScript().getCode());
            case JAVASCRIPT_WITH_SCOPE -> {
                BsonJavaScriptWithScope javaScript = value.asJavaScriptWithScope();
                node.setText(javaScript.getCode());
                node.setChildren(encodeFields(javaScript.getScope()));
            }
            case SYMBOL -> node.setText(value.asSymbol().getSymbol());
            case DB_POINTER -> node.setText(toDbPointerText(value));
            case MIN_KEY -> node.setText("MinKey");
            case MAX_KEY -> node.setText("MaxKey");
            default -> node.setText(value.toString());
        }
        return node;
    }

    private static String toDbPointerText(BsonValue value) {
        if (value instanceof BsonDbPointer pointer) {
            return "DBPointer(\"" + pointer.getNamespace() + "\", " + pointer.getId().toHexString() + ")";
        }
        return value.toString();
    }
}
