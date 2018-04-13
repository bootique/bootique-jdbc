package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.metrics.health.HealthCheckOutcome;
import io.bootique.metrics.health.HealthCheckRegistry;
import io.bootique.metrics.health.HealthCheckStatus;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class ConnectivityCheckIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testHealthChecks() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/healthcheck/ConnectivityCheckIT.yml")
                .autoLoadModules()
                .createRuntime();
        
        // trigger DataSource creation
        runtime.getInstance(DataSourceFactory.class).forName("db");

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);
        HealthCheckOutcome beforeShutdown = registry.runHealthCheck(ConnectivityCheck.healthCheckName("db"));
        assertEquals(HealthCheckStatus.OK, beforeShutdown.getStatus());

        shutdownDerby();

        HealthCheckOutcome afterShutdown = registry.runHealthCheck(ConnectivityCheck.healthCheckName("db"));
        assertEquals(HealthCheckStatus.CRITICAL, afterShutdown.getStatus());
    }

    private void shutdownDerby() {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            // the exception is actually expected on shutdown... go figure...
        }
    }
}
