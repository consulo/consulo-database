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
 * A BSON timestamp - the internal replication type, not a date: seconds since the epoch and an ordinal within the second, both
 * unsigned.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public record MongoTimestampValue(int time, int increment) implements Comparable<MongoTimestampValue> {
    /**
     * @param packed the seconds in the high 32 bits, the increment in the low 32 bits
     */
    public static MongoTimestampValue of(long packed) {
        return new MongoTimestampValue((int) (packed >>> 32), (int) packed);
    }

    @Override
    public int compareTo(MongoTimestampValue o) {
        int result = Integer.compareUnsigned(time, o.time);
        return result != 0 ? result : Integer.compareUnsigned(increment, o.increment);
    }

    @Override
    public String toString() {
        return "Timestamp(" + Integer.toUnsignedString(time) + ", " + Integer.toUnsignedString(increment) + ")";
    }
}
