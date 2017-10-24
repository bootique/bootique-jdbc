package io.bootique.jdbc;

import java.util.Optional;

/**
 * A listener attached to {@link DataSourceFactory} that is notified about DataSource start and stop events.
 * Implementors are usually managing an in-memory database and would perform the needed steps to prepare and cleanup
 * such DB.
 *
 * @since 0.12
 */
public interface DataSourceListener<T extends javax.sql.DataSource> {

    /**
     * @param name
     * @param jdbcUrl
     */
    void beforeStartup(String name, Optional<String> jdbcUrl);

    /**
     * @param name
     * @param jdbcUrl
     * @param dataSource
     * @deprecated since 0.25
     */
    void afterStartup(String name, Optional<String> jdbcUrl, T dataSource);

    /**
     * @param name
     * @param dataSource
     * @since 0.25
     */
    default void afterStartup(String name, T dataSource) {
        //stub
    }

    /**
     * @param name
     * @param jdbcUrl
     * @deprecated since 0.25
     */
    void afterShutdown(String name, Optional<String> jdbcUrl);

    /**
     * @param name
     * @param dataSource
     * @since 0.25
     */
    default void afterShutdown(String name, T dataSource) {
        //stub
    }
}
