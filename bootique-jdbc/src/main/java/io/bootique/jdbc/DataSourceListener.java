package io.bootique.jdbc;

import javax.sql.DataSource;

/**
 * A listener attached to {@link DataSourceFactory} that is notified about DataSource start and stop events.
 * Implementors are usually managing an in-memory database and would perform the needed steps to prepare and cleanup
 * such DB.
 *
 * @since 0.25
 */
public interface DataSourceListener {

    void beforeStartup(String name, String jdbcUrl);

    void afterStartup(String name, String jdbcUrl, DataSource dataSource);

    void afterShutdown(String name, String jdbcUrl, DataSource dataSource);
}
