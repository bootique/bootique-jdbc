package io.bootique.jdbc;

import javax.sql.DataSource;

/**
 * A listener attached to {@link DataSourceFactory} that is notified about DataSource start and stop events.
 *
 * @since 0.25
 */
public interface DataSourceListener {

    void beforeStartup(String name, String jdbcUrl);

    void afterStartup(String name, String jdbcUrl, DataSource dataSource);

    void afterShutdown(String name, String jdbcUrl, DataSource dataSource);
}
