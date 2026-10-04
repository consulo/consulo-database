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

import java.util.Locale;

/**
 * A BSON ObjectId as its 24 hex digits.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public record MongoObjectId(String hexString) implements Comparable<MongoObjectId> {
    public MongoObjectId {
        hexString = hexString.toLowerCase(Locale.ROOT);
    }

    /**
     * Lower case hex digits of equal length sort like the bytes of the ObjectId - creation time first.
     */
    @Override
    public int compareTo(MongoObjectId o) {
        return hexString.compareTo(o.hexString);
    }

    @Override
    public String toString() {
        return hexString;
    }
}
