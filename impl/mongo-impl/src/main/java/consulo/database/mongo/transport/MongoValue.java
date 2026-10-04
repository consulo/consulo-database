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

import java.util.List;
import java.util.Objects;

/**
 * An immutable BSON value on the IDE side, decoded from the tree the runtime process sends ({@link MongoNodeDecoder}). Scalars hold
 * the Java value {@link MongoValueMapper} documents; a document holds its fields in document order, an array its items.
 * <p>
 * Values compare structurally: two documents with the same fields in the same order are equal, so reloading the same documents
 * compares equal and an {@code _id} can be deduplicated.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public final class MongoValue {
    public static final MongoValue NULL = new MongoValue(MongoValueKind.NULL, null, null, List.of(), List.of());

    public static final MongoValue UNDEFINED = new MongoValue(MongoValueKind.UNDEFINED, null, null, List.of(), List.of());

    private static final int MAX_TO_STRING_LENGTH = 200;

    private final MongoValueKind myKind;
    private final @Nullable Object myScalar;
    private final @Nullable String myOtherTypeName;
    private final List<MongoField> myFields;
    private final List<MongoValue> myItems;

    private int myHashCode;

    private MongoValue(MongoValueKind kind,
                       @Nullable Object scalar,
                       @Nullable String otherTypeName,
                       List<MongoField> fields,
                       List<MongoValue> items) {
        myKind = kind;
        myScalar = scalar;
        myOtherTypeName = otherTypeName;
        myFields = fields;
        myItems = items;
    }

    /**
     * @param kind  a scalar kind - not null, undefined, document, array or other
     * @param value the Java value of the kind, see {@link MongoValueMapper}
     */
    public static MongoValue scalar(MongoValueKind kind, Object value) {
        switch (kind) {
            case NULL, UNDEFINED, DOCUMENT, ARRAY, OTHER -> throw new IllegalArgumentException("Not a scalar kind: " + kind);
            default -> {
                return new MongoValue(kind, value, null, List.of(), List.of());
            }
        }
    }

    /**
     * A value of a type without a Java counterpart - regex, javascript, symbol and alike - kept as its display text.
     *
     * @param typeName the {@code $type} alias, for example {@code regex}
     */
    public static MongoValue other(String typeName, String text) {
        return new MongoValue(MongoValueKind.OTHER, text, typeName, List.of(), List.of());
    }

    public static MongoValue document(List<MongoField> fields) {
        return new MongoValue(MongoValueKind.DOCUMENT, null, null, List.copyOf(fields), List.of());
    }

    public static MongoValue array(List<MongoValue> items) {
        return new MongoValue(MongoValueKind.ARRAY, null, null, List.of(), List.copyOf(items));
    }

    public MongoValueKind getKind() {
        return myKind;
    }

    /**
     * @return the type alias of the MongoDB {@code $type} operator, {@code uuid} for a UUID binary
     */
    public String getTypeName() {
        String typeName = myKind.getTypeName();
        if (typeName != null) {
            return typeName;
        }
        return myOtherTypeName == null ? "unknown" : myOtherTypeName;
    }

    /**
     * @return whether the value is {@code null} or {@code undefined}
     */
    public boolean isNull() {
        return myKind == MongoValueKind.NULL || myKind == MongoValueKind.UNDEFINED;
    }

    public boolean isDocument() {
        return myKind == MongoValueKind.DOCUMENT;
    }

    public boolean isArray() {
        return myKind == MongoValueKind.ARRAY;
    }

    /**
     * @return the Java value of a scalar, the display text of an {@link MongoValueKind#OTHER} value; {@code null} for null, undefined,
     * documents and arrays
     */
    public @Nullable Object getScalar() {
        return myScalar;
    }

    /**
     * @return the fields of a document in document order, empty for any other kind
     */
    public List<MongoField> getFields() {
        return myFields;
    }

    /**
     * @return the items of an array, empty for any other kind
     */
    public List<MongoValue> getItems() {
        return myItems;
    }

    /**
     * @return the value of the first field with the name, {@code null} when the document has none - or this is no document
     */
    public @Nullable MongoValue get(String name) {
        for (MongoField field : myFields) {
            if (field.name().equals(name)) {
                return field.value();
            }
        }
        return null;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MongoValue that)) {
            return false;
        }

        return myKind == that.myKind &&
            Objects.equals(myScalar, that.myScalar) &&
            Objects.equals(myOtherTypeName, that.myOtherTypeName) &&
            myFields.equals(that.myFields) &&
            myItems.equals(that.myItems);
    }

    @Override
    public int hashCode() {
        int hashCode = myHashCode;
        if (hashCode == 0) {
            hashCode = Objects.hash(myKind, myScalar, myOtherTypeName, myFields, myItems);
            myHashCode = hashCode;
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return MongoValueText.format(this, MAX_TO_STRING_LENGTH);
    }
}
