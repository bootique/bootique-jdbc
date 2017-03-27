package io.bootique.jdbc.test.csv;

import io.bootique.jdbc.test.Column;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Converts String values coming from CSV to Java value objects.
 *
 * @since 0.14
 */
public class ValueConverter {

    // same as ISO, except there's space between date and time

    private static DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Object fromString(String value, Column column) {

        if (value == null) {
            return null;
        }

        if (value.length() == 0) {
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

        switch (column.getType()) {
            case Types.INTEGER:
                return Integer.valueOf(value);
            case Types.BIGINT:
                return Long.valueOf(value);
            case Types.DATE:
                return Date.valueOf(LocalDate.parse(value));
            case Types.TIME:
                return Time.valueOf(LocalTime.parse(value));
            case Types.TIMESTAMP:
                return Timestamp.valueOf(LocalDateTime.parse(value, DATE_TIME_FORMAT));
            // TODO: other conversions...
            default:
                return value;
        }
    }

    private static boolean isChar(int jdbcType) {
        switch (jdbcType) {
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.CLOB:
                return true;
            default:
                return false;
        }
    }


}
