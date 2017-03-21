package io.bootique.jdbc.test.csv;

import io.bootique.jdbc.test.Column;

/**
 * Converts String values coming from CSV to Java value objects.
 *
 * @since 0.14
 */
public class ValueConverter {

    public Object fromString(String value, Column column) {

        // TODO: on Derby inserting CSV Strings in DATE and TIMESTAMP columns seems to work... No guarantee it will work
        // on other DBs
        return value == null || value.length() == 0 ? null : value;
    }
}
