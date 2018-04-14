package io.bootique.jdbc.instrumented.tomcat.healthcheck;

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

    private DataSource dataSource;

    public DataSourceHealthCheck(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public HealthCheckOutcome check() throws Exception {

        try (Connection c = dataSource.getConnection()) {
            return c.isValid(1)
                    ? HealthCheckOutcome.ok()
                    : HealthCheckOutcome.critical("Connection validation failed");
        }
    }

    /**
     * Generates stable qualified name for the {@link DataSourceHealthCheck}
     *
     * @param dataSourceName
     * @return qualified name bq.jdbc.[dataSourceName].canConnect
     */
    public static String healthCheckName(String dataSourceName) {
        return "bq.jdbc." + dataSourceName + ".canConnect";
    }
}
