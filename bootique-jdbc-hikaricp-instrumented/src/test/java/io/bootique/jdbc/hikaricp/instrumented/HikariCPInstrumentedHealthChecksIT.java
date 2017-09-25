package io.bootique.jdbc.hikaricp.instrumented;

import io.bootique.BQRuntime;
import io.bootique.metrics.health.HealthCheckOutcome;
import io.bootique.metrics.health.HealthCheckRegistry;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HikariCPInstrumentedHealthChecksIT {
    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testDataSourceHealthCheckGroupRun() {

        BQRuntime runtime = testFactory.app("-c", "classpath:dummy-1ds.yml").autoLoadModules().createRuntime();
        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);

        Map<String, HealthCheckOutcome> result = registry.runHealthChecks();

        assertEquals(1, result.size());
        assertTrue(result.get("ds").isHealthy());
    }

    @Test
    public void testDataSourcesHealthCheckCroupRun() {

        BQRuntime runtime = testFactory.app("-c", "classpath:dummy-2ds.yml").autoLoadModules().createRuntime();
        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);

        Map<String, HealthCheckOutcome> result = registry.runHealthChecks();

        assertEquals(2, result.size());
        assertTrue(result.get("ds1").isHealthy());
        assertTrue(result.get("ds2").isHealthy());
    }
}
