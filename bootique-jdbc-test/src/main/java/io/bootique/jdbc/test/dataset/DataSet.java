package io.bootique.jdbc.test.dataset;

/**
 * @since 0.24
 */
public interface DataSet {

    /**
     * Inserts data set records to the underlying DB.
     */
    void persist();
}
