package io.bootique.jdbc;

import javax.sql.DataSource;

/**
 * A listener attached to {@link DataSourceFactory} that is notified about DataSource start and stop events.
 * Implementors are usually managing an in-memory database and would perform the needed steps to prepare and cleanup
 * such DB.
 *
 * @since 0.25
 */
public interface DataSourceListener<T extends javax.sql.DataSource> {

    void beforeStartup(String name, String jdbcUrl);

    void afterStartup(String name, T dataSource);

    /**
     * @param name
     * @param jdbcUrl
     * @param dataSource
     * @deprecated since 0.25
     */
    void afterStartup(String name, String jdbcUrl, DataSource dataSource);

    void afterShutdown(String name, T dataSource);

    /**
     * @param name
     * @param jdbcUrl
     * @deprecated since 0.25
     */
    void afterShutdown(String name, String jdbcUrl);
}
