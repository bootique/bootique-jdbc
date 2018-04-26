package io.bootique.jdbc.instrumented.tomcat.healthcheck;

import io.bootique.jdbc.instrumented.tomcat.JdbcTomcatInstrumentedModule;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.HealthCheckOutcome;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Checks DataSource health. Are connections available? Are they valid?
 *
 * @since 0.12
 */
public class TomcatConnectivityCheck implements HealthCheck {

    private DataSource dataSource;

    public TomcatConnectivityCheck(DataSource dataSource) {
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
     * Generates stable qualified name for the {@link TomcatConnectivityCheck}
     *
     * @param dataSourceName
     * @return qualified name bq.JdbcTomcat.Pool.[dataSourceName].Connectivity
     */
    public static String healthCheckName(String dataSourceName) {
        return JdbcTomcatInstrumentedModule.METRIC_NAMING.name("Pool", dataSourceName, "Connectivity");
    }
}
