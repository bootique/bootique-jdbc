package io.bootique.jdbc.instrumented.healthcheck;

import io.bootique.jdbc.DataSourceFactory;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.HealthCheckOutcome;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Checks DataSource health. Are connections available? Are they valid?
 *
 * @since 0.12
 */
public class DataSourceHealthCheck implements HealthCheck {

    private DataSourceFactory dataSourceFactory;
    private String dataSourceName;

    public DataSourceHealthCheck(DataSourceFactory dataSourceFactory, String dataSourceName) {
        // storing both the factory and the name instead of the DataSource to avoid premature
        // triggering of DataSource creation.
        this.dataSourceFactory = dataSourceFactory;
        this.dataSourceName = dataSourceName;
    }

    @Override
    public HealthCheckOutcome check() throws Exception {

        DataSource ds = dataSourceFactory.forName(dataSourceName);
        try (Connection c = ds.getConnection()) {
            return c.isValid(1)
                    ? HealthCheckOutcome.ok()
                    : HealthCheckOutcome.critical("Connection validation failed");
        }
    }
}
