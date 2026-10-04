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

import java.sql.Types;
import java.util.Locale;

/**
 * How the values of a result set column are read, chosen once per column from its metadata.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
enum JdbcColumnKind {
    BOOLEAN,
    INT,
    LONG,
    UNSIGNED_LONG,
    DOUBLE,
    DECIMAL,
    STRING,
    CLOB,
    XML,
    DATE,
    TIME,
    TIME_WITH_TIME_ZONE,
    TIMESTAMP,
    TIMESTAMP_WITH_TIME_ZONE,
    BYTES,
    BLOB,
    ARRAY,
    /**
     * Read with getObject() and sent by the class of the object
     */
    OBJECT,
    /**
     * Sent as text with the class of getObject()
     */
    OTHER;

    static JdbcColumnKind of(int jdbcType, String typeName, int precision) {
        String lowerTypeName = typeName.toLowerCase(Locale.ROOT);
        boolean unsigned = lowerTypeName.contains("unsigned");

        return switch (jdbcType) {
            // a BIT of more than one bit is a bit string: the drivers disagree about its object (byte[], String)
            case Types.BIT -> precision <= 1 ? BOOLEAN : OBJECT;
            case Types.BOOLEAN -> BOOLEAN;
            case Types.TINYINT, Types.SMALLINT -> INT;
            case Types.INTEGER -> unsigned ? LONG : INT;
            case Types.BIGINT -> unsigned ? UNSIGNED_LONG : LONG;
            case Types.REAL, Types.FLOAT, Types.DOUBLE -> DOUBLE;
            case Types.NUMERIC, Types.DECIMAL -> DECIMAL;
            case Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR, Types.NCHAR, Types.NVARCHAR, Types.LONGNVARCHAR -> STRING;
            case Types.CLOB, Types.NCLOB -> CLOB;
            case Types.SQLXML -> XML;
            case Types.DATE -> DATE;
            // some drivers report zoned types with the plain constant, for example PostgreSQL reports timetz as TIME
            case Types.TIME -> isZoned(lowerTypeName) ? TIME_WITH_TIME_ZONE : TIME;
            case Types.TIME_WITH_TIMEZONE -> TIME_WITH_TIME_ZONE;
            case Types.TIMESTAMP -> isZoned(lowerTypeName) ? TIMESTAMP_WITH_TIME_ZONE : TIMESTAMP;
            case Types.TIMESTAMP_WITH_TIMEZONE -> TIMESTAMP_WITH_TIME_ZONE;
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> BYTES;
            case Types.BLOB -> BLOB;
            case Types.ARRAY -> ARRAY;
            default -> OTHER;
        };
    }

    private static boolean isZoned(String lowerTypeName) {
        return lowerTypeName.equals("timetz") || lowerTypeName.equals("timestamptz") || lowerTypeName.contains("with time zone");
    }
}
