package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.metrics.health.HealthCheckData;
import io.bootique.metrics.health.HealthCheckOutcome;
import io.bootique.metrics.health.HealthCheckRegistry;
import io.bootique.metrics.health.HealthCheckStatus;
import io.bootique.test.junit.BQTestFactory;
import io.bootique.value.Duration;
import org.junit.Rule;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Connection99PercentCheckIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testHealthChecks() throws SQLException {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/healthcheck/Connection99PercentCheckIT.yml")
                .autoLoadModules()
                .createRuntime();

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);
        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("db");

        HealthCheckOutcome outcome = registry.runHealthCheck(Connection99PercentCheck.healthCheckName("db"));
        assertEquals(HealthCheckStatus.OK, outcome.getStatus());
        HealthCheckData<Duration> data = (HealthCheckData<Duration>) outcome.getData().get();
        assertTrue(data.getValue().getDuration().toMillis() == 0);

        // checkout a few connections....
        for (int i = 0; i < 5; i++) {
            try (Connection c = ds.getConnection()) {
            }
        }

        outcome = registry.runHealthCheck(Connection99PercentCheck.healthCheckName("db"));
        assertEquals(outcome.getMessage(), HealthCheckStatus.OK, outcome.getStatus());

        data = (HealthCheckData<Duration>) outcome.getData().get();
        assertNotNull(data);

        // note that the underlying metric is rounded to milliseconds, so it is often == 0.
        // as a result we can't effectively test the metric payload...
        // assertTrue(data.getValue().getDuration().toMillis() > 0);

        // shutdown Derby
        shutdownDerby();

        outcome = registry.runHealthCheck(ConnectivityCheck.healthCheckName("db"));
        assertEquals(HealthCheckStatus.CRITICAL, outcome.getStatus());
    }

    private void shutdownDerby() {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            // the exception is actually expected on shutdown... go figure...
        }
    }
}
