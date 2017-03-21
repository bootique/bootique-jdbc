package io.bootique.jdbc.test.csv;

import io.bootique.jdbc.test.Column;

import java.sql.Types;

/**
 * Converts String values coming from CSV to Java value objects.
 *
 * @since 0.14
 */
public class ValueConverter {

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

        // TODO: other conversions...
        return value;
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
