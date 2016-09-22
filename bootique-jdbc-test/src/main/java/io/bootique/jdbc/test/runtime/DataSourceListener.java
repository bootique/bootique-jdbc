package io.bootique.jdbc.test.runtime;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * A listener attached to {@link TestDataSourceFactory} that is notified about DataSource start and stop events.
 * Implementors are usually managing an in-memory database and would perform the needed steps to prepare and cleanup
 * such DB.
 *
 * @since 0.12
 */
public interface DataSourceListener {

    void beforeStartup(String name, Optional<String> jdbcUrl);

    void afterStartup(String name, Optional<String> jdbcUrl, DataSource dataSource);

    void afterShutdown(String name, Optional<String> jdbcUrl);
}
