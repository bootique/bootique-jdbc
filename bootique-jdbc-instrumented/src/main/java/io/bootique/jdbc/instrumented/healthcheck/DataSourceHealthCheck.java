package io.bootique.jdbc.instrumented.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import io.bootique.jdbc.DataSourceFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Checks DataSource health. Are connections available? Are they valid?
 *
 * @since 0.12
 */
public class DataSourceHealthCheck extends HealthCheck {

    private DataSourceFactory dataSourceFactory;
    private String dataSourceName;

    public DataSourceHealthCheck(DataSourceFactory dataSourceFactory, String dataSourceName) {
        // storing both the factory and the name instead of the DataSource to avoid premature
        // triggering of DataSource creation.
        this.dataSourceFactory = dataSourceFactory;
        this.dataSourceName = dataSourceName;
    }

    @Override
    protected Result check() throws Exception {

        try (Connection c = dataSourceFactory.forName(dataSourceName).getConnection()) {
            return c.isValid(1)
                    ? HealthCheck.Result.healthy()
                    : HealthCheck.Result.unhealthy("Connection validation failed");
        } catch (SQLException e) {
            return Result.unhealthy(e);
        }
    }
}
