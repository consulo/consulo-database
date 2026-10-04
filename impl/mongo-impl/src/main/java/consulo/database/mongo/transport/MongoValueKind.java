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

/**
 * The BSON type of a {@link MongoValue}, as the IDE side knows it. The runtime process sends the same kinds over the wire; the IDE
 * never loads a BSON class.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public enum MongoValueKind {
    NULL(MongoValueMapper.TYPE_NULL),
    UNDEFINED(MongoValueMapper.TYPE_UNDEFINED),
    BOOL(MongoValueMapper.TYPE_BOOL),
    INT32(MongoValueMapper.TYPE_INT),
    INT64(MongoValueMapper.TYPE_LONG),
    DOUBLE(MongoValueMapper.TYPE_DOUBLE),
    DECIMAL128(MongoValueMapper.TYPE_DECIMAL),
    STRING(MongoValueMapper.TYPE_STRING),
    DATE(MongoValueMapper.TYPE_DATE),
    TIMESTAMP(MongoValueMapper.TYPE_TIMESTAMP),
    OBJECT_ID(MongoValueMapper.TYPE_OBJECT_ID),
    UUID(MongoValueMapper.TYPE_UUID),
    BINARY(MongoValueMapper.TYPE_BINARY),
    DOCUMENT(MongoValueMapper.TYPE_OBJECT),
    ARRAY(MongoValueMapper.TYPE_ARRAY),
    /**
     * regex, javascript, javascriptWithScope, symbol, dbPointer, minKey, maxKey, or a type a newer runtime knows - the value carries
     * its own type alias.
     */
    OTHER(null);

    private final @Nullable String myTypeName;

    MongoValueKind(@Nullable String typeName) {
        myTypeName = typeName;
    }

    /**
     * @return the type alias of the MongoDB {@code $type} operator ({@code uuid} for a UUID binary), {@code null} for {@link #OTHER}
     */
    public @Nullable String getTypeName() {
        return myTypeName;
    }
}
