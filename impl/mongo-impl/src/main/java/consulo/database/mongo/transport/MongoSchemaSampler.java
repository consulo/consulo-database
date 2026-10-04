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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collects the top level fields of documents: their union, in the order the documents have them, with the BSON types each field
 * held and whether it was present in every document.
 * <p>
 * The field order merges the key orders of the documents: a field first seen in a later document is placed after the field it
 * follows there, not appended. {@code _id} always comes first.
 * <p>
 * Documents come in whole - the fetched page - or as shapes: the top level field names and types of the random sample, which the
 * runtime process reads ({@code $sample}) so that the IDE never receives the sampled documents themselves.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public final class MongoSchemaSampler {
    public static final String ID_FIELD = "_id";

    /**
     * How many random documents are sampled in addition to the first page.
     */
    public static final int SAMPLE_SIZE = 200;

    /**
     * A top level field of a document shape.
     *
     * @param typeName the type alias of the value, as {@link MongoValue#getTypeName()} names it
     */
    public record FieldType(String name, String typeName) {
    }

    private static final class FieldAccumulator {
        private final Set<String> myTypeNames = new LinkedHashSet<>();
        private int myCount;

        void add(String typeName) {
            myTypeNames.add(typeName);
            myCount++;
        }
    }

    private final List<String> myOrder = new ArrayList<>();
    private final Map<String, FieldAccumulator> myFields = new HashMap<>();
    private final Set<MongoValue> mySeenIds = new HashSet<>();
    private int myDocumentCount;

    /**
     * Adds a document. A document whose {@code _id} was added before is skipped, so a random sample may overlap the first page.
     */
    public void add(MongoValue document) {
        List<MongoField> fields = document.getFields();
        List<FieldType> types = new ArrayList<>(fields.size());
        for (MongoField field : fields) {
            types.add(new FieldType(field.name(), field.value().getTypeName()));
        }
        addFields(document.get(ID_FIELD), types);
    }

    /**
     * Adds the shape of a sampled document, skipped as {@link #add} skips a document.
     *
     * @param id     the value of {@code _id}, {@code null} when the document has none
     * @param fields the top level fields in document order
     */
    public void addShape(@Nullable MongoValue id, List<FieldType> fields) {
        addFields(id, fields);
    }

    private void addFields(@Nullable MongoValue id, List<FieldType> fields) {
        if (id != null && !mySeenIds.add(id)) {
            return;
        }

        myDocumentCount++;

        Set<String> names = new HashSet<>();
        @Nullable String previous = null;
        for (FieldType fieldType : fields) {
            String name = fieldType.name();
            if (!names.add(name)) {
                // a name repeated in one document - BSON allows it - counts once, as the first one is the value of the field
                continue;
            }

            FieldAccumulator field = myFields.get(name);
            if (field == null) {
                field = new FieldAccumulator();
                myFields.put(name, field);

                int index = previous == null ? 0 : myOrder.indexOf(previous) + 1;
                myOrder.add(index, name);
            }

            field.add(fieldType.typeName());
            previous = name;
        }
    }

    public int getDocumentCount() {
        return myDocumentCount;
    }

    public List<MongoFieldInfo> getFields() {
        List<MongoFieldInfo> fields = new ArrayList<>(myOrder.size());

        FieldAccumulator idField = myFields.get(ID_FIELD);
        if (idField != null) {
            fields.add(createInfo(ID_FIELD, idField));
        }

        for (String name : myOrder) {
            if (ID_FIELD.equals(name)) {
                continue;
            }

            FieldAccumulator field = myFields.get(name);
            if (field != null) {
                fields.add(createInfo(name, field));
            }
        }
        return fields;
    }

    private MongoFieldInfo createInfo(String name, FieldAccumulator field) {
        boolean presentEverywhere = field.myCount == myDocumentCount;
        return new MongoFieldInfo(name, new LinkedHashSet<>(field.myTypeNames), presentEverywhere);
    }
}
