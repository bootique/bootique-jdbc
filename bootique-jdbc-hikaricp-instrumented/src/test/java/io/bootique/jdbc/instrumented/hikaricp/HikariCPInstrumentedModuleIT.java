package io.bootique.jdbc.instrumented.hikaricp;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariDataSource;
import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.instrumented.healthcheck.DataSourceHealthCheck;
import io.bootique.jdbc.instrumented.hikaricp.healthcheck.Connection99PercentCheck;
import io.bootique.jdbc.instrumented.hikaricp.healthcheck.ConnectivityCheck;
import io.bootique.metrics.health.HealthCheckOutcome;
import io.bootique.metrics.health.HealthCheckRegistry;
import io.bootique.test.junit.BQTestFactory;
import org.junit.ClassRule;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HikariCPInstrumentedModuleIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();

    static final String METRIC_CATEGORY = "pool";
    static final String METRIC_NAME_WAIT = "Wait";
    static final String METRIC_NAME_TOTAL_CONNECTIONS = "TotalConnections";
    static final String METRIC_NAME_IDLE_CONNECTIONS = "IdleConnections";
    static final String METRIC_NAME_ACTIVE_CONNECTIONS = "ActiveConnections";
    static final String METRIC_NAME_PENDING_CONNECTIONS = "PendingConnections";

    @Test
    public void testMetrics_TurnedOn() {
        BQRuntime runtime = TEST_FACTORY.app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/hikaricp-ds-health.yml")
                .autoLoadModules()
                .createRuntime();

        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);

        DataSource ds = factory.forName("derby1");
        assertNotNull(ds);
        assertTrue(ds instanceof HikariDataSource);

        HikariDataSource hikariDS = (HikariDataSource) ds;
        String poolName = hikariDS.getPoolName();

        MetricRegistry metricRegistry = runtime.getInstance(MetricRegistry.class);
        assertEquals(metricRegistry.getTimers().size(), 1);
        assertEquals(metricRegistry.getTimers().firstKey(),
                MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_WAIT));

        assertEquals(metricRegistry.getGauges().size(), 4);
        assertEquals(metricRegistry.getGauges().keySet(), new HashSet<String>() {{
            add(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_TOTAL_CONNECTIONS));
            add(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_IDLE_CONNECTIONS));
            add(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_ACTIVE_CONNECTIONS));
            add(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_PENDING_CONNECTIONS));
        }});
    }

    @Test
    public void testHealthChecks_TurnedOn() {
        BQRuntime runtime = TEST_FACTORY.app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/hikaricp-ds-health.yml")
                .autoLoadModules()
                .createRuntime();

        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);

        String dataSourceName = "derby1";
        HikariDataSource dataSource = (HikariDataSource) factory.forName(dataSourceName);
        assertNotNull(dataSource);

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);

        assertTrue(registry.containsHealthCheck(ConnectivityCheck.healthCheckName(dataSourceName)));
        assertTrue(registry.containsHealthCheck(Connection99PercentCheck.healthCheckName(dataSourceName)));
        /**
         * embedded health check {@link io.bootique.jdbc.instrumented.healthcheck.DataSourceHealthCheck}
         */
        assertTrue(registry.containsHealthCheck(DataSourceHealthCheck.healthCheckName(dataSourceName)));

        Map<String, HealthCheckOutcome> results = registry.runHealthChecks();
        assertEquals(results.size(), 3);
    }

    @Test
    public void testHealthChecks_HealthNoParam_ChecksTurnedOff() {
        BQRuntime runtime = TEST_FACTORY.app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/dummy-ds.yml")
                .autoLoadModules()
                .createRuntime();

        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);
        String dataSourceName = "DerbyDatabaseIT";

        HikariDataSource dataSource = (HikariDataSource) factory.forName(dataSourceName);
        assertNotNull(dataSource);

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);
        /**
         * embedded health check {@link io.bootique.jdbc.instrumented.healthcheck.DataSourceHealthCheck}
         */
        assertTrue(registry.containsHealthCheck(DataSourceHealthCheck.healthCheckName(dataSourceName)));


        Map<String, HealthCheckOutcome> results = registry.runHealthChecks();
        assertEquals(results.size(), 1);
    }

    @Test
    public void testHealthChecksMultipleDs() throws SQLException {

        BQRuntime runtime = TEST_FACTORY.app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/hikaricp-ds2-health.yml")
                .autoLoadModules()
                .createRuntime();

        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);
        String derby2 = "derby2", derby3 = "derby3";
        HikariDataSource ds2 = (HikariDataSource) factory.forName(derby2);
        assertNotNull(ds2);

        HikariDataSource ds3 = (HikariDataSource) factory.forName(derby3);
        assertNotNull(ds3);

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);

        assertTrue(registry.containsHealthCheck(ConnectivityCheck.healthCheckName(derby2)));
        assertTrue(registry.containsHealthCheck(Connection99PercentCheck.healthCheckName(derby2)));
        /**
         * embedded health check {@link io.bootique.jdbc.instrumented.healthcheck.DataSourceHealthCheck}
         */
        assertTrue(registry.containsHealthCheck(DataSourceHealthCheck.healthCheckName(derby2)));

        assertTrue(registry.containsHealthCheck(ConnectivityCheck.healthCheckName(derby3)));
        assertTrue(registry.containsHealthCheck(Connection99PercentCheck.healthCheckName(derby3)));
        /**
         * embedded health check {@link io.bootique.jdbc.instrumented.healthcheck.DataSourceHealthCheck}
         */
        assertTrue(registry.containsHealthCheck(DataSourceHealthCheck.healthCheckName(derby3)));

        Map<String, HealthCheckOutcome> results = registry.runHealthChecks();
        assertEquals(results.size(), 6);
    }
}