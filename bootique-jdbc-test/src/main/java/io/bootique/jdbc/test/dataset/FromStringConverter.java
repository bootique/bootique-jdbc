package io.bootique.jdbc.test.dataset;

import io.bootique.jdbc.test.Column;

/**
 * @since 0.24
 */
public interface FromStringConverter {

    Object fromString(String value, Column column);
}
