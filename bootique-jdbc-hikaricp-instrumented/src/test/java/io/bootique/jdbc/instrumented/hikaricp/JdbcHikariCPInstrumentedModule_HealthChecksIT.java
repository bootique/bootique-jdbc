package io.bootique.jdbc.instrumented.hikaricp;

import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
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

        // fault DataSource to init metrics
        runtime.getInstance(DataSourceFactory.class).forName("db");

        Set<String> names = runtime.getInstance(HealthCheckRegistry.class).healthCheckNames();

        Set<String> expectedNames = new HashSet<>(asList(
                "bq.JdbcHikariCP.Pool.db.Connectivity",
                "bq.JdbcHikariCP.Pool.db.Wait99Percent"));
        assertEquals(expectedNames, names);

    }
}
