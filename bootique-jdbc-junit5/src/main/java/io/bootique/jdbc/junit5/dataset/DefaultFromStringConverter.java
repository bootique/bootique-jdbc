/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.jdbc.junit5.dataset;

import io.bootique.jdbc.junit5.metadata.DbColumnMetadata;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;

/**
 * Converts String values coming from CSV to Java value objects.
 */
public class DefaultFromStringConverter implements FromStringConverter {

    static final FromStringConverter DEFAULT_CONVERTER = new DefaultFromStringConverter();

    @Override
    public Object fromString(String value, DbColumnMetadata column) {

        if (value == null) {
            return null;
        }

        if (value.isEmpty()) {
            // this is an empty String for char columns and a null for all others
            return isChar(column.getType()) ? "" : null;
        }

        // for the purpose of SQL scripts, "NULL" String is null
        // TODO: a more universal escape sequence for nulls
        if (value.equals("NULL")) {
            return null;
        }

        if (isChar(column.getType())) {
            return value;
        }

        return switch (column.getType()) {
            case Types.INTEGER -> Integer.valueOf(value);
            case Types.BIGINT -> Long.valueOf(value);
            case Types.DECIMAL, Types.NUMERIC -> new BigDecimal(value);
            case Types.DATE -> Date.valueOf(LocalDate.parse(value));
            case Types.TIME -> Time.valueOf(LocalTime.parse(value));
            // The format is ISO-8601: yyyy-MM-ddTHH:mm:ss
            case Types.TIMESTAMP -> Timestamp.valueOf(LocalDateTime.parse(value));
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB -> Base64.getDecoder().decode(value);
            case Types.BIT, Types.BOOLEAN -> Boolean.valueOf(value);
            // TODO: other conversions...
            default -> value;
        };
    }

    private static boolean isChar(int jdbcType) {
        return switch (jdbcType) {
            case Types.VARCHAR, Types.CHAR, Types.CLOB -> true;
            default -> false;
        };
    }


}
