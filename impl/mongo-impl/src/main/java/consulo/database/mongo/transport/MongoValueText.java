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

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One line display text of {@link MongoValue}s in MongoDB shell syntax: {@code {"city": "Lviv", "geo": {"lat": 50.01}}},
 * {@code ObjectId("…")}, {@code UUID("…")}. The text is bounded - a big document is never written out completely for one line.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public final class MongoValueText {
    private static final char ELLIPSIS = '…';

    private MongoValueText() {
    }

    /**
     * @param maxLength the text is cut after so many characters and ends with {@code …} then
     */
    public static String format(MongoValue value, int maxLength) {
        StringBuilder builder = new StringBuilder();
        append(builder, value, maxLength);
        if (builder.length() > maxLength) {
            builder.setLength(maxLength);
            builder.append(ELLIPSIS);
        }
        return builder.toString();
    }

    /**
     * @param value a value of {@link MongoValueMapper#toJavaValue} which is not a container
     */
    public static String formatScalar(@Nullable Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        if (value instanceof MongoObjectId objectId) {
            return "ObjectId(\"" + objectId.hexString() + "\")";
        }
        if (value instanceof UUID uuid) {
            return "UUID(\"" + uuid + "\")";
        }
        // Instant prints ISO-8601 in UTC, MongoBinaryValue a bounded hex, MongoTimestampValue Timestamp(t, i), MongoDecimalValue
        // its text - NaN, Infinity, -0
        return String.valueOf(value);
    }

    private static void append(StringBuilder builder, MongoValue value, int maxLength) {
        if (builder.length() > maxLength) {
            return;
        }

        // by kind, not by the class of the scalar: a string and the display text of a regex are both a String, only the first is
        // quoted
        switch (value.getKind()) {
            case DOCUMENT -> appendDocument(builder, value, maxLength);
            case ARRAY -> appendArray(builder, value, maxLength);
            case STRING -> appendString(builder, String.valueOf(value.getScalar()), maxLength);
            case OTHER -> builder.append(value.getScalar());
            default -> builder.append(formatScalar(MongoValueMapper.toJavaValue(value)));
        }
    }

    private static void appendDocument(StringBuilder builder, MongoValue document, int maxLength) {
        builder.append('{');
        boolean first = true;
        for (MongoField field : document.getFields()) {
            if (builder.length() > maxLength) {
                return;
            }

            if (!first) {
                builder.append(", ");
            }
            first = false;

            appendString(builder, field.name(), maxLength);
            builder.append(": ");
            append(builder, field.value(), maxLength);
        }
        builder.append('}');
    }

    private static void appendArray(StringBuilder builder, MongoValue array, int maxLength) {
        builder.append('[');
        boolean first = true;
        for (MongoValue item : array.getItems()) {
            if (builder.length() > maxLength) {
                return;
            }

            if (!first) {
                builder.append(", ");
            }
            first = false;

            append(builder, item, maxLength);
        }
        builder.append(']');
    }

    private static void appendString(StringBuilder builder, String value, int maxLength) {
        builder.append('"');
        for (int i = 0; i < value.length(); i++) {
            if (builder.length() > maxLength) {
                return;
            }

            char c = value.charAt(i);
            switch (c) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (c < 0x20) {
                        builder.append(String.format("\\u%04x", (int) c));
                    }
                    else {
                        builder.append(c);
                    }
                }
            }
        }
        builder.append('"');
    }
}
