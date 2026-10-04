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

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * A top level field of the documents of a collection, as {@link MongoSchemaSampler} saw it.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public final class MongoFieldInfo {
    private static final Set<String> INTEGER_TYPES = Set.of(MongoValueMapper.TYPE_INT, MongoValueMapper.TYPE_LONG);
    private static final Set<String> FLOAT_TYPES =
        Set.of(MongoValueMapper.TYPE_INT, MongoValueMapper.TYPE_LONG, MongoValueMapper.TYPE_DOUBLE);
    private static final Set<String> DECIMAL_TYPES =
        Set.of(MongoValueMapper.TYPE_INT, MongoValueMapper.TYPE_LONG, MongoValueMapper.TYPE_DOUBLE, MongoValueMapper.TYPE_DECIMAL);

    private final String myName;
    private final Set<String> myTypeNames;
    private final boolean myPresentEverywhere;
    private final MongoColumnKind myKind;
    private final String myTypeName;

    MongoFieldInfo(String name, Set<String> typeNames, boolean presentEverywhere) {
        myName = name;
        myTypeNames = Collections.unmodifiableSet(new TreeSet<>(typeNames));
        myPresentEverywhere = presentEverywhere;

        Set<String> valueTypeNames = new TreeSet<>(typeNames);
        valueTypeNames.remove(MongoValueMapper.TYPE_NULL);
        valueTypeNames.remove(MongoValueMapper.TYPE_UNDEFINED);

        if (valueTypeNames.isEmpty()) {
            // only null seen
            myKind = MongoColumnKind.ANY;
            myTypeName = String.join("|", myTypeNames);
        }
        else {
            myKind = computeKind(valueTypeNames);
            myTypeName = String.join("|", valueTypeNames);
        }
    }

    /**
     * The kind of a column whose values have several types: {int, long} is INTEGER, int or long with double is FLOAT, any numeric mix
     * with decimal is DECIMAL, every other mix is ANY. A value of the column is never coerced to that kind.
     */
    private static MongoColumnKind computeKind(Set<String> valueTypeNames) {
        if (valueTypeNames.size() == 1) {
            return MongoColumnKind.ofTypeName(valueTypeNames.iterator().next());
        }
        if (INTEGER_TYPES.containsAll(valueTypeNames)) {
            return MongoColumnKind.INTEGER;
        }
        if (FLOAT_TYPES.containsAll(valueTypeNames)) {
            return MongoColumnKind.FLOAT;
        }
        if (DECIMAL_TYPES.containsAll(valueTypeNames)) {
            return MongoColumnKind.DECIMAL;
        }
        return MongoColumnKind.ANY;
    }

    public String getName() {
        return myName;
    }

    /**
     * @return every type alias seen in the field, sorted, {@code null} included
     */
    public Set<String> getTypeNames() {
        return myTypeNames;
    }

    /**
     * @return whether every sampled document has the field - {@code false} when some miss it, which differs from holding {@code null}
     */
    public boolean isPresentEverywhere() {
        return myPresentEverywhere;
    }

    public MongoColumnKind getKind() {
        return myKind;
    }

    /**
     * @return the type aliases without {@code null}, joined by {@code |} - for example {@code double|int}; {@code null} for a field
     * which only held null
     */
    public String getTypeName() {
        return myTypeName;
    }

    @Override
    public String toString() {
        return myName + ": " + myKind + " " + myTypeName + (myPresentEverywhere ? "" : " (sometimes missing)");
    }
}
