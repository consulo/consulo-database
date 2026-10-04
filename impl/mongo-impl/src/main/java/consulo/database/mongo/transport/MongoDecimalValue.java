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
 * A BSON decimal128 which has no {@code BigDecimal}: NaN, {@code Infinity}, {@code -Infinity} and negative zero. Every other
 * decimal is a {@code BigDecimal}. It is still a {@link Number}, so a decimal column holds numbers only.
 * <p>
 * The order compares {@link #doubleValue()}; values with different texts may compare as equal, for example {@code -0} and
 * {@code -0E+3}.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public final class MongoDecimalValue extends Number implements Comparable<MongoDecimalValue> {
    private static final long serialVersionUID = 1L;

    private final String myText;
    private final double myDoubleValue;

    /**
     * @param text the decimal128 text, for example {@code NaN} or {@code -0E+3}
     */
    public MongoDecimalValue(String text) {
        myText = text;
        myDoubleValue = parse(text);
    }

    private static double parse(String text) {
        try {
            // NaN, Infinity and -Infinity are written the same way in Java, and -0 with any exponent parses as -0.0
            return Double.parseDouble(text);
        }
        catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    public String getText() {
        return myText;
    }

    @Override
    public int intValue() {
        return (int) myDoubleValue;
    }

    @Override
    public long longValue() {
        return (long) myDoubleValue;
    }

    @Override
    public float floatValue() {
        return (float) myDoubleValue;
    }

    /**
     * @return {@code NaN}, an infinity or {@code -0.0}
     */
    @Override
    public double doubleValue() {
        return myDoubleValue;
    }

    @Override
    public int compareTo(MongoDecimalValue o) {
        return Double.compare(myDoubleValue, o.myDoubleValue);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return this == o || o instanceof MongoDecimalValue that && myText.equals(that.myText);
    }

    @Override
    public int hashCode() {
        return myText.hashCode();
    }

    @Override
    public String toString() {
        return myText;
    }
}
