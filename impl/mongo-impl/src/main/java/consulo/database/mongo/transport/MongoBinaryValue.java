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

import java.util.Arrays;

/**
 * A BSON binary value which is not a UUID. Unlike a {@code byte[]} it is equal to another value with the same subtype and bytes,
 * so reloading the same documents compares equal.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public final class MongoBinaryValue {
    private static final int MAX_DISPLAY_BYTES = 64;

    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    private final byte mySubType;
    private final byte[] myData;

    public MongoBinaryValue(byte subType, byte[] data) {
        mySubType = subType;
        myData = data.clone();
    }

    /**
     * @return the BSON binary subtype, for example {@code 0} for generic or {@code 3} for a legacy UUID - whose byte order depends on
     * the driver which wrote it, so it is not decoded
     */
    public byte getSubType() {
        return mySubType;
    }

    public byte[] getData() {
        return myData.clone();
    }

    public int getLength() {
        return myData.length;
    }

    /**
     * @return {@code 0x}-prefixed hex of at most {@code maxBytes} bytes, followed by {@code …} when there are more
     */
    public String toHexString(int maxBytes) {
        int count = Math.min(myData.length, maxBytes);
        StringBuilder builder = new StringBuilder(2 + count * 2 + 1);
        builder.append("0x");
        for (int i = 0; i < count; i++) {
            int value = myData[i] & 0xFF;
            builder.append(HEX_DIGITS[value >>> 4]).append(HEX_DIGITS[value & 0x0F]);
        }

        if (count < myData.length) {
            builder.append('…');
        }
        return builder.toString();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        return o instanceof MongoBinaryValue that && mySubType == that.mySubType && Arrays.equals(myData, that.myData);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(myData) + mySubType;
    }

    @Override
    public String toString() {
        return toHexString(MAX_DISPLAY_BYTES);
    }
}
