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

package consulo.database.jdbc.rt;

import consulo.database.jdbc.rt.shared.JdbcValue;
import consulo.database.jdbc.rt.shared.JdbcValueType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a result set value into a {@link JdbcValue}.
 * <p>
 * Every typed read falls back: java.time objects need JDBC 4.2 support of the driver, so the java.sql getter is tried next,
 * and a value which the driver can not convert to the type of its column goes as its text ({@link JdbcValueType#_string}).
 * <p>
 * Values without offset are converted to the zone of the process, which is the zone of the IDE (the process is started with
 * -Duser.timezone).
 *
 * @author VISTALL
 * @since 2026-10-04
 */
final class JdbcValueReader {
    // stops an array which contains itself
    private static final int MAX_ARRAY_DEPTH = 32;

    private JdbcValueReader() {
    }

    static JdbcValue read(ResultSet resultSet, int index, JdbcColumnKind kind) throws SQLException {
        try {
            return readTyped(resultSet, index, kind);
        }
        catch (SQLException | RuntimeException | AbstractMethodError e) {
            return stringValue(resultSet.getString(index));
        }
    }

    private static JdbcValue readTyped(ResultSet resultSet, int index, JdbcColumnKind kind) throws SQLException {
        switch (kind) {
            case BOOLEAN: {
                boolean value = resultSet.getBoolean(index);
                return resultSet.wasNull() ? nullValue() : new JdbcValue(JdbcValueType._bool).setBoolValue(value);
            }
            case INT: {
                int value = resultSet.getInt(index);
                return resultSet.wasNull() ? nullValue() : new JdbcValue(JdbcValueType._int).setIntValue(value);
            }
            case LONG: {
                long value = resultSet.getLong(index);
                return resultSet.wasNull() ? nullValue() : new JdbcValue(JdbcValueType._long).setLongValue(value);
            }
            case DOUBLE: {
                double value = resultSet.getDouble(index);
                return resultSet.wasNull() ? nullValue() : new JdbcValue(JdbcValueType._double).setDoubleValue(value);
            }
            case UNSIGNED_LONG:
            case DECIMAL: {
                BigDecimal value = resultSet.getBigDecimal(index);
                return value == null ? nullValue() : textValue(JdbcValueType._decimal, value.toPlainString());
            }
            case STRING:
                return stringValue(resultSet.getString(index));
            case CLOB:
                return stringValue(readClob(resultSet, index));
            case XML:
                return stringValue(readXml(resultSet, index));
            case DATE:
                return dateValue(readDate(resultSet, index));
            case TIME:
                return timeValue(readTime(resultSet, index));
            case TIME_WITH_TIME_ZONE: {
                OffsetTime value = resultSet.getObject(index, OffsetTime.class);
                return value == null ? nullValue() : textValue(JdbcValueType._time, value.toString());
            }
            case TIMESTAMP:
                return timestampValue(readTimestamp(resultSet, index));
            case TIMESTAMP_WITH_TIME_ZONE:
                return timestampWithZoneValue(readTimestampWithZone(resultSet, index));
            case BYTES: {
                byte[] value = resultSet.getBytes(index);
                return value == null ? nullValue() : new JdbcValue(JdbcValueType._bytes).setBytesValue(value);
            }
            case BLOB: {
                byte[] value = readBlob(resultSet, index);
                return value == null ? nullValue() : new JdbcValue(JdbcValueType._bytes).setBytesValue(value);
            }
            case ARRAY:
                return readArray(resultSet, index);
            case OBJECT:
                return encodeObject(resultSet.getObject(index), 0);
            case OTHER:
            default:
                return readOther(resultSet, index);
        }
    }

    private static LocalDate readDate(ResultSet resultSet, int index) throws SQLException {
        try {
            return resultSet.getObject(index, LocalDate.class);
        }
        catch (SQLException | RuntimeException | AbstractMethodError e) {
            java.sql.Date value = resultSet.getDate(index);
            return value == null ? null : value.toLocalDate();
        }
    }

    private static LocalTime readTime(ResultSet resultSet, int index) throws SQLException {
        try {
            return resultSet.getObject(index, LocalTime.class);
        }
        catch (SQLException | RuntimeException | AbstractMethodError e) {
            Time value = resultSet.getTime(index);
            return value == null ? null : value.toLocalTime();
        }
    }

    private static LocalDateTime readTimestamp(ResultSet resultSet, int index) throws SQLException {
        try {
            return resultSet.getObject(index, LocalDateTime.class);
        }
        catch (SQLException | RuntimeException | AbstractMethodError e) {
            Timestamp value = resultSet.getTimestamp(index);
            return value == null ? null : value.toLocalDateTime();
        }
    }

    private static OffsetDateTime readTimestampWithZone(ResultSet resultSet, int index) throws SQLException {
        try {
            return resultSet.getObject(index, OffsetDateTime.class);
        }
        catch (SQLException | RuntimeException | AbstractMethodError e) {
            Timestamp value = resultSet.getTimestamp(index);
            return value == null ? null : OffsetDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
        }
    }

    private static String readClob(ResultSet resultSet, int index) throws SQLException {
        try {
            return resultSet.getString(index);
        }
        catch (SQLException | RuntimeException e) {
            Clob clob = resultSet.getClob(index);
            return clob == null ? null : readClob(clob);
        }
    }

    private static String readClob(Clob clob) throws SQLException {
        try {
            long length = clob.length();
            return length == 0 ? "" : clob.getSubString(1, (int) Math.min(length, Integer.MAX_VALUE));
        }
        finally {
            free(clob);
        }
    }

    private static String readXml(ResultSet resultSet, int index) throws SQLException {
        try {
            return resultSet.getString(index);
        }
        catch (SQLException | RuntimeException e) {
            SQLXML xml = resultSet.getSQLXML(index);
            return xml == null ? null : readXml(xml);
        }
    }

    private static String readXml(SQLXML xml) throws SQLException {
        try {
            return xml.getString();
        }
        finally {
            free(xml);
        }
    }

    private static byte[] readBlob(ResultSet resultSet, int index) throws SQLException {
        try {
            return resultSet.getBytes(index);
        }
        catch (SQLException | RuntimeException e) {
            Blob blob = resultSet.getBlob(index);
            return blob == null ? null : readBlob(blob);
        }
    }

    private static byte[] readBlob(Blob blob) throws SQLException {
        try {
            long length = blob.length();
            return length == 0 ? new byte[0] : blob.getBytes(1, (int) Math.min(length, Integer.MAX_VALUE));
        }
        finally {
            free(blob);
        }
    }

    private static JdbcValue readArray(ResultSet resultSet, int index) throws SQLException {
        java.sql.Array array = resultSet.getArray(index);
        return array == null ? nullValue() : encodeArray(array, 0);
    }

    private static JdbcValue encodeArray(java.sql.Array array, int depth) throws SQLException {
        try {
            return encodeElements(array.getArray(), depth);
        }
        finally {
            free(array);
        }
    }

    private static JdbcValue encodeElements(Object elements, int depth) throws SQLException {
        if (elements == null) {
            return nullValue();
        }

        if (depth >= MAX_ARRAY_DEPTH || !elements.getClass().isArray()) {
            return otherValue(String.valueOf(elements), elements.getClass().getName());
        }

        // a driver may return an array of primitives, so the elements are read by reflection
        int length = java.lang.reflect.Array.getLength(elements);
        List<JdbcValue> values = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            values.add(encodeObject(java.lang.reflect.Array.get(elements, i), depth + 1));
        }
        return new JdbcValue(JdbcValueType._array).setArrayValue(values);
    }

    /**
     * The value of an array element, or of a column whose type does not tell how to read it, by the class of the object
     */
    private static JdbcValue encodeObject(Object value, int depth) throws SQLException {
        if (value == null) {
            return nullValue();
        }

        if (value instanceof Boolean booleanValue) {
            return new JdbcValue(JdbcValueType._bool).setBoolValue(booleanValue);
        }
        if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return new JdbcValue(JdbcValueType._int).setIntValue(((Number) value).intValue());
        }
        if (value instanceof Long longValue) {
            return new JdbcValue(JdbcValueType._long).setLongValue(longValue);
        }
        if (value instanceof Double || value instanceof Float) {
            return new JdbcValue(JdbcValueType._double).setDoubleValue(((Number) value).doubleValue());
        }
        if (value instanceof BigDecimal decimal) {
            return textValue(JdbcValueType._decimal, decimal.toPlainString());
        }
        if (value instanceof BigInteger integer) {
            return textValue(JdbcValueType._decimal, integer.toString());
        }
        if (value instanceof String || value instanceof Character) {
            return stringValue(value.toString());
        }
        if (value instanceof byte[] bytes) {
            return new JdbcValue(JdbcValueType._bytes).setBytesValue(bytes);
        }
        // Timestamp and java.sql.Date extend java.util.Date - check them before any other date class
        if (value instanceof Timestamp timestamp) {
            return timestampValue(timestamp.toLocalDateTime());
        }
        if (value instanceof java.sql.Date date) {
            return dateValue(date.toLocalDate());
        }
        if (value instanceof Time time) {
            return timeValue(time.toLocalTime());
        }
        if (value instanceof LocalDate date) {
            return dateValue(date);
        }
        if (value instanceof LocalTime time) {
            return timeValue(time);
        }
        if (value instanceof OffsetTime time) {
            return textValue(JdbcValueType._time, time.toString());
        }
        if (value instanceof LocalDateTime dateTime) {
            return timestampValue(dateTime);
        }
        if (value instanceof OffsetDateTime dateTime) {
            return timestampWithZoneValue(dateTime);
        }
        if (value instanceof ZonedDateTime dateTime) {
            return timestampWithZoneValue(dateTime.toOffsetDateTime());
        }
        if (value instanceof Instant instant) {
            return timestampWithZoneValue(OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()));
        }
        if (value instanceof java.sql.Array array) {
            return depth >= MAX_ARRAY_DEPTH ? otherValue(array.toString(), array.getClass().getName()) : encodeArray(array, depth);
        }
        if (value instanceof Clob clob) {
            return stringValue(readClob(clob));
        }
        if (value instanceof Blob blob) {
            return new JdbcValue(JdbcValueType._bytes).setBytesValue(readBlob(blob));
        }
        if (value instanceof SQLXML xml) {
            return stringValue(readXml(xml));
        }
        if (value.getClass().isArray()) {
            return encodeElements(value, depth);
        }
        return otherValue(value.toString(), value.getClass().getName());
    }

    private static JdbcValue readOther(ResultSet resultSet, int index) throws SQLException {
        Object value = resultSet.getObject(index);
        if (value == null) {
            return nullValue();
        }

        String text;
        if (value instanceof String stringValue) {
            text = stringValue;
        }
        else {
            try {
                text = resultSet.getString(index);
            }
            catch (SQLException | RuntimeException e) {
                text = null;
            }

            if (text == null) {
                text = value.toString();
            }
        }
        return otherValue(text, value.getClass().getName());
    }

    /**
     * @return true when the ISO-8601 time text ends with an offset (+02:00, -05:00, Z)
     */
    static boolean hasOffset(String timeText) {
        return timeText.indexOf('+') >= 0 || timeText.indexOf('-') >= 0 || timeText.endsWith("Z");
    }

    private static JdbcValue dateValue(LocalDate value) {
        return value == null ? nullValue() : textValue(JdbcValueType._date, value.toString());
    }

    private static JdbcValue timeValue(LocalTime value) {
        return value == null ? nullValue() : textValue(JdbcValueType._time, value.toString());
    }

    private static JdbcValue timestampValue(LocalDateTime value) {
        return value == null ? nullValue() : textValue(JdbcValueType._timestamp, value.toString());
    }

    private static JdbcValue timestampWithZoneValue(OffsetDateTime value) {
        return value == null ? nullValue() : textValue(JdbcValueType._timestamptz, value.toString());
    }

    private static JdbcValue stringValue(String value) {
        return value == null ? nullValue() : textValue(JdbcValueType._string, value);
    }

    private static JdbcValue otherValue(String text, String className) {
        return textValue(JdbcValueType._other, text).setClassName(className);
    }

    private static JdbcValue textValue(JdbcValueType type, String text) {
        return new JdbcValue(type).setStringValue(text);
    }

    private static JdbcValue nullValue() {
        return new JdbcValue(JdbcValueType._null);
    }

    private static void free(java.sql.Array array) {
        try {
            array.free();
        }
        catch (SQLException | RuntimeException | AbstractMethodError ignored) {
        }
    }

    private static void free(Clob clob) {
        try {
            clob.free();
        }
        catch (SQLException | RuntimeException | AbstractMethodError ignored) {
        }
    }

    private static void free(Blob blob) {
        try {
            blob.free();
        }
        catch (SQLException | RuntimeException | AbstractMethodError ignored) {
        }
    }

    private static void free(SQLXML xml) {
        try {
            xml.free();
        }
        catch (SQLException | RuntimeException | AbstractMethodError ignored) {
        }
    }
}
