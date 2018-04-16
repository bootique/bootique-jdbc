package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.metrics.health.HealthCheckOutcome;
import io.bootique.metrics.health.HealthCheckRegistry;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HikariCPHealthCheckGroupFactoryIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testHealthChecks() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/ds-health.yml")
                .autoLoadModules()
                .createRuntime();

        String dataSourceName = "derby1";

        // trigger DataSource creation
        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);
        factory.forName(dataSourceName);

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);

        Map<String, HealthCheckOutcome> results = registry.runHealthChecks();
        assertEquals(2, results.size());

        assertTrue(registry.containsHealthCheck(ConnectivityCheck.healthCheckName(dataSourceName)));
        assertTrue(registry.containsHealthCheck(Connection99PercentCheck.healthCheckName(dataSourceName)));
    }

    @Test
    public void testHealthChecks_Implicit() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/ds-nohealth.yml")
                .autoLoadModules()
                .createRuntime();

        String dataSourceName = "DerbyDatabaseIT";

        // trigger DataSource creation...
        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);
        factory.forName(dataSourceName);

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);
        Map<String, HealthCheckOutcome> results = registry.runHealthChecks();
        assertEquals(2, results.size());

        assertTrue(registry.containsHealthCheck(ConnectivityCheck.healthCheckName(dataSourceName)));
        assertTrue(registry.containsHealthCheck(Connection99PercentCheck.healthCheckName(dataSourceName)));
    }

    @Test
    public void testHealthChecksMultipleDs() {

        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/ds2-health.yml")
                .autoLoadModules()
                .createRuntime();

        String derby2 = "derby2";
        String derby3 = "derby3";

        // trigger DataSource creation
        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);
        factory.forName(derby2);
        factory.forName(derby3);

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);

        assertTrue(registry.containsHealthCheck(ConnectivityCheck.healthCheckName(derby2)));
        assertTrue(registry.containsHealthCheck(Connection99PercentCheck.healthCheckName(derby2)));

        assertTrue(registry.containsHealthCheck(ConnectivityCheck.healthCheckName(derby3)));
        assertTrue(registry.containsHealthCheck(Connection99PercentCheck.healthCheckName(derby3)));

        Map<String, HealthCheckOutcome> results = registry.runHealthChecks();
        assertEquals(results.size(), 4);
    }
}
