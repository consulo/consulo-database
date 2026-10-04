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

/**
 * The kind of a column of MongoDB documents, named like the constants of {@code consulo.ui.grid.GridTypeKind} it maps to once the
 * data grid is published.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public enum MongoColumnKind {
    TEXT,
    INTEGER,
    FLOAT,
    DECIMAL,
    BOOLEAN,
    TIMESTAMP_TZ,
    UUID,
    BINARY,
    DOCUMENT,
    ARRAY,
    OTHER,
    /**
     * Values of several types, or only {@code null} - each cell value decides how it is shown.
     */
    ANY;

    /**
     * @param typeName a BSON type alias of {@link MongoValueMapper#getTypeName}
     */
    public static MongoColumnKind ofTypeName(String typeName) {
        return switch (typeName) {
            case MongoValueMapper.TYPE_STRING, MongoValueMapper.TYPE_REGEX, MongoValueMapper.TYPE_JAVASCRIPT,
                 MongoValueMapper.TYPE_JAVASCRIPT_WITH_SCOPE, MongoValueMapper.TYPE_SYMBOL -> TEXT;
            case MongoValueMapper.TYPE_INT, MongoValueMapper.TYPE_LONG -> INTEGER;
            case MongoValueMapper.TYPE_DOUBLE -> FLOAT;
            case MongoValueMapper.TYPE_DECIMAL -> DECIMAL;
            case MongoValueMapper.TYPE_BOOL -> BOOLEAN;
            case MongoValueMapper.TYPE_DATE -> TIMESTAMP_TZ;
            case MongoValueMapper.TYPE_UUID -> UUID;
            case MongoValueMapper.TYPE_BINARY -> BINARY;
            case MongoValueMapper.TYPE_OBJECT -> DOCUMENT;
            case MongoValueMapper.TYPE_ARRAY -> ARRAY;
            case MongoValueMapper.TYPE_NULL, MongoValueMapper.TYPE_UNDEFINED -> ANY;
            default -> OTHER;
        };
    }
}
