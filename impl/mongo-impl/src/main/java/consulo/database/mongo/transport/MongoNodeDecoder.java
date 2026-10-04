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

import consulo.database.mongo.rt.shared.MongoNode;
import consulo.database.mongo.rt.shared.MongoNodeKind;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Decodes the value tree of the runtime process ({@link MongoNode}) into {@link MongoValue}s.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public final class MongoNodeDecoder {
    private static final String UNKNOWN_TYPE_NAME = "unknown";

    private MongoNodeDecoder() {
    }

    /**
     * @param node a node of a {@code DOCUMENT} kind
     */
    public static MongoValue decodeDocument(MongoNode node) {
        if (node.getKind() != MongoNodeKind.DOCUMENT) {
            throw new IllegalStateException("Not a document: " + node.getKind());
        }
        return decode(node);
    }

    public static MongoValue decode(MongoNode node) {
        MongoValue value = decodeOrNull(node);
        // a missing value never comes as a root, only as a positional cell - read it as undefined rather than fail
        return value == null ? MongoValue.UNDEFINED : value;
    }

    /**
     * @return the value, {@code null} for {@link MongoNodeKind#MISSING}
     */
    private static @Nullable MongoValue decodeOrNull(MongoNode node) {
        MongoNodeKind kind = node.getKind();
        if (kind == null) {
            // a kind of a newer runtime
            return MongoValue.other(node.isSetTypeName() ? node.getTypeName() : UNKNOWN_TYPE_NAME, text(node));
        }

        return switch (kind) {
            case MISSING -> null;
            case NULL -> MongoValue.NULL;
            case UNDEFINED -> MongoValue.UNDEFINED;
            case BOOL -> MongoValue.scalar(MongoValueKind.BOOL, Boolean.valueOf(node.isBoolValue()));
            case INT32 -> MongoValue.scalar(MongoValueKind.INT32, Integer.valueOf(node.getIntValue()));
            case INT64 -> MongoValue.scalar(MongoValueKind.INT64, Long.valueOf(node.getLongValue()));
            case DOUBLE -> MongoValue.scalar(MongoValueKind.DOUBLE, Double.valueOf(node.getDoubleValue()));
            case DECIMAL128 -> MongoValue.scalar(MongoValueKind.DECIMAL128, decodeDecimal(text(node)));
            case STRING -> MongoValue.scalar(MongoValueKind.STRING, text(node));
            case DATE -> MongoValue.scalar(MongoValueKind.DATE, Instant.ofEpochMilli(node.getLongValue()));
            case TIMESTAMP -> MongoValue.scalar(MongoValueKind.TIMESTAMP, MongoTimestampValue.of(node.getLongValue()));
            case OBJECT_ID -> MongoValue.scalar(MongoValueKind.OBJECT_ID, new MongoObjectId(text(node)));
            case UUID -> decodeUuid(text(node));
            case BINARY -> MongoValue.scalar(MongoValueKind.BINARY, decodeBinary(node));
            case DOCUMENT -> MongoValue.document(decodeFields(node));
            case ARRAY -> MongoValue.array(decodeItems(node));
            case OTHER -> decodeOther(node);
        };
    }

    /**
     * @param kind          the kind of a value, {@code null} for a kind of a newer runtime
     * @param otherTypeName the type alias the runtime sent along with {@link MongoNodeKind#OTHER}
     * @return the type alias of the MongoDB {@code $type} operator, as {@link MongoValue#getTypeName()} names it
     */
    public static String typeName(@Nullable MongoNodeKind kind, @Nullable String otherTypeName) {
        MongoValueKind valueKind = kind == null ? null : toValueKind(kind);
        String typeName = valueKind == null ? null : valueKind.getTypeName();
        if (typeName != null) {
            return typeName;
        }
        return otherTypeName == null ? UNKNOWN_TYPE_NAME : otherTypeName;
    }

    private static @Nullable MongoValueKind toValueKind(MongoNodeKind kind) {
        return switch (kind) {
            case MISSING -> null;
            case NULL -> MongoValueKind.NULL;
            case UNDEFINED -> MongoValueKind.UNDEFINED;
            case BOOL -> MongoValueKind.BOOL;
            case INT32 -> MongoValueKind.INT32;
            case INT64 -> MongoValueKind.INT64;
            case DOUBLE -> MongoValueKind.DOUBLE;
            case DECIMAL128 -> MongoValueKind.DECIMAL128;
            case STRING -> MongoValueKind.STRING;
            case DATE -> MongoValueKind.DATE;
            case TIMESTAMP -> MongoValueKind.TIMESTAMP;
            case OBJECT_ID -> MongoValueKind.OBJECT_ID;
            case UUID -> MongoValueKind.UUID;
            case BINARY -> MongoValueKind.BINARY;
            case DOCUMENT -> MongoValueKind.DOCUMENT;
            case ARRAY -> MongoValueKind.ARRAY;
            case OTHER -> MongoValueKind.OTHER;
        };
    }

    private static List<MongoField> decodeFields(MongoNode node) {
        if (!node.isSetChildren()) {
            return List.of();
        }

        List<MongoField> fields = new ArrayList<>(node.getChildrenSize());
        for (MongoNode child : node.getChildren()) {
            MongoValue value = decodeOrNull(child);
            if (value != null) {
                String name = child.getName();
                fields.add(new MongoField(name == null ? "" : name, value));
            }
        }
        return fields;
    }

    private static List<MongoValue> decodeItems(MongoNode node) {
        if (!node.isSetChildren()) {
            return List.of();
        }

        List<MongoValue> items = new ArrayList<>(node.getChildrenSize());
        for (MongoNode child : node.getChildren()) {
            MongoValue value = decodeOrNull(child);
            if (value != null) {
                items.add(value);
            }
        }
        return items;
    }

    /**
     * @return a {@code BigDecimal}, or a {@link MongoDecimalValue} for NaN, the infinities and negative zero, which have none
     */
    private static Object decodeDecimal(String text) {
        try {
            BigDecimal decimal = new BigDecimal(text);
            if (decimal.signum() == 0 && text.startsWith("-")) {
                return new MongoDecimalValue(text);
            }
            return decimal;
        }
        catch (NumberFormatException e) {
            return new MongoDecimalValue(text);
        }
    }

    private static MongoValue decodeUuid(String text) {
        try {
            return MongoValue.scalar(MongoValueKind.UUID, java.util.UUID.fromString(text));
        }
        catch (IllegalArgumentException e) {
            return MongoValue.other(MongoValueMapper.TYPE_UUID, text);
        }
    }

    private static MongoBinaryValue decodeBinary(MongoNode node) {
        byte[] bytes = node.isSetBytes() ? node.getBytes() : null;
        return new MongoBinaryValue(node.getBinarySubtype(), bytes == null ? new byte[0] : bytes);
    }

    /**
     * Keeps the display text of M0: {@code /pattern/flags} for a regex, the code of javascript - the scope of javascriptWithScope,
     * which comes as children, is not shown.
     */
    private static MongoValue decodeOther(MongoNode node) {
        String typeName = node.isSetTypeName() ? node.getTypeName() : UNKNOWN_TYPE_NAME;
        if (MongoValueMapper.TYPE_REGEX.equals(typeName)) {
            String options = node.isSetOptions() ? node.getOptions() : "";
            return MongoValue.other(typeName, "/" + text(node) + "/" + options);
        }
        return MongoValue.other(typeName, text(node));
    }

    private static String text(MongoNode node) {
        String text = node.getText();
        return text == null ? "" : text;
    }
}
