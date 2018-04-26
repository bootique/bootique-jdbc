package io.bootique.jdbc.instrumented.hikaricp;

import io.bootique.BQRuntime;
import io.bootique.metrics.health.HealthCheckRegistry;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class JdbcHikariCPInstrumentedModule_HealthChecksIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testHealthChecks() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/JdbcHikariCPInstrumentedModule_HealthChecksIT.yml")
                .autoLoadModules()
                .createRuntime();

        Set<String> expectedNames = new HashSet<>(asList(
                "bq.JdbcHikariCP.Pool.db.Connectivity",
                "bq.JdbcHikariCP.Pool.db.Wait99Percent"));

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);
        assertEquals(expectedNames, registry.healthCheckNames());
    }

    @Test
    public void testHealthChecksMultipleDs() {

        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/JdbcHikariCPInstrumentedModule_HealthChecksIT_multi.yml")
                .autoLoadModules()
                .createRuntime();

        Set<String> expectedNames = new HashSet<>(asList(
                "bq.JdbcHikariCP.Pool.db1.Connectivity",
                "bq.JdbcHikariCP.Pool.db1.Wait99Percent",
                "bq.JdbcHikariCP.Pool.db2.Connectivity",
                "bq.JdbcHikariCP.Pool.db2.Wait99Percent"));

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);
        assertEquals(expectedNames, registry.healthCheckNames());
    }

    @Test
    public void testHealthChecks_Implicit() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/JdbcHikariCPInstrumentedModule_HealthChecksIT_no_health.yml")
                .autoLoadModules()
                .createRuntime();

        Set<String> expectedNames = new HashSet<>(asList(
                "bq.JdbcHikariCP.Pool.db.Connectivity",
                "bq.JdbcHikariCP.Pool.db.Wait99Percent"));

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);
        assertEquals(expectedNames, registry.healthCheckNames());
    }
}
