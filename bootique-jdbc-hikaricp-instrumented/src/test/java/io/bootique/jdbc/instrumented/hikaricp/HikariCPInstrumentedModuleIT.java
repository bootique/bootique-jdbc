package io.bootique.jdbc.instrumented.hikaricp;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.zaxxer.hikari.HikariDataSource;
import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.DataSourceListener;
import io.bootique.metrics.health.HealthCheckOutcome;
import io.bootique.metrics.health.HealthCheckRegistry;
import io.bootique.test.junit.BQTestFactory;
import org.junit.ClassRule;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.bootique.jdbc.instrumented.hikaricp.HikariCPMetricsInitializer.METRIC_CATEGORY;
import static io.bootique.jdbc.instrumented.hikaricp.HikariCPMetricsInitializer.METRIC_NAME_ACTIVE_CONNECTIONS;
import static io.bootique.jdbc.instrumented.hikaricp.HikariCPMetricsInitializer.METRIC_NAME_IDLE_CONNECTIONS;
import static io.bootique.jdbc.instrumented.hikaricp.HikariCPMetricsInitializer.METRIC_NAME_PENDING_CONNECTIONS;
import static io.bootique.jdbc.instrumented.hikaricp.HikariCPMetricsInitializer.METRIC_NAME_TOTAL_CONNECTIONS;
import static io.bootique.jdbc.instrumented.hikaricp.HikariCPMetricsInitializer.METRIC_NAME_WAIT;
import static io.bootique.jdbc.instrumented.hikaricp.healthcheck.HikariCPHealthCheckGroupFactory.CHECK_CATEGORY;
import static io.bootique.jdbc.instrumented.hikaricp.healthcheck.HikariCPHealthCheckGroupFactory.CONNECTION_99_PERCENT;
import static io.bootique.jdbc.instrumented.hikaricp.healthcheck.HikariCPHealthCheckGroupFactory.CONNECTIVITY_CHECK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HikariCPInstrumentedModuleIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();

    @Test
    public void testMetricsListener_Injected() {
        BQRuntime runtime = TEST_FACTORY.app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/dummy-ds.yml")
                .autoLoadModules()
                .createRuntime();

        TypeLiteral<Set<DataSourceListener>> typeLiteral = new TypeLiteral<Set<DataSourceListener>>() {
        };

        Set<DataSourceListener> set = runtime.getInstance(Key.get(typeLiteral));
        assertEquals(set.size(), 1);
        assertTrue(set.iterator().next() instanceof HikariCPMetricsInitializer);
    }

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

        HikariDataSource dataSource = (HikariDataSource) factory.forName("derby1");
        assertNotNull(dataSource);

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);
        String poolName = dataSource.getPoolName();

        assertTrue(registry.containsHealthCheck(MetricRegistry.name(poolName, CHECK_CATEGORY, CONNECTIVITY_CHECK)));
        assertTrue(registry.containsHealthCheck(MetricRegistry.name(poolName, CHECK_CATEGORY, CONNECTION_99_PERCENT)));
        /**
         * embedded health check {@link io.bootique.jdbc.instrumented.healthcheck.DataSourceHealthCheck}
         */
        assertTrue(registry.containsHealthCheck("derby1"));

        Map<String, HealthCheckOutcome> results = registry.runHealthChecks();
        assertEquals(results.size(), 3);
    }

    @Test
    public void testHealthChecks_HealthNoParam_ChecksTurnedOff() {
        BQRuntime runtime = TEST_FACTORY.app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/dummy-ds.yml")
                .autoLoadModules()
                .createRuntime();

        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);

        HikariDataSource dataSource = (HikariDataSource) factory.forName("DerbyDatabaseIT");
        assertNotNull(dataSource);

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);
        /**
         * embedded health check {@link io.bootique.jdbc.instrumented.healthcheck.DataSourceHealthCheck}
         */
        assertTrue(registry.containsHealthCheck("DerbyDatabaseIT"));


        Map<String, HealthCheckOutcome> results = registry.runHealthChecks();
        assertEquals(results.size(), 1);
    }

    @Test
    public void testHealthChecksMultipleDs() throws SQLException {

        BQRuntime runtime = TEST_FACTORY.app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/hikaricp-ds2-health.yml")
                .autoLoadModules()
                .createRuntime();

        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);

        HikariDataSource ds2 = (HikariDataSource) factory.forName("derby2");
        assertNotNull(ds2);

        HikariDataSource ds3 = (HikariDataSource) factory.forName("derby3");
        assertNotNull(ds3);

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);
        String poolName2 = ds2.getPoolName();
        String poolName3 = ds3.getPoolName();

        assertTrue(registry.containsHealthCheck(MetricRegistry.name(poolName2, CHECK_CATEGORY, CONNECTIVITY_CHECK)));
        assertTrue(registry.containsHealthCheck(MetricRegistry.name(poolName2, CHECK_CATEGORY, CONNECTION_99_PERCENT)));
        /**
         * embedded health check {@link io.bootique.jdbc.instrumented.healthcheck.DataSourceHealthCheck}
         */
        assertTrue(registry.containsHealthCheck("derby2"));

        assertTrue(registry.containsHealthCheck(MetricRegistry.name(poolName3, CHECK_CATEGORY, CONNECTIVITY_CHECK)));
        assertTrue(registry.containsHealthCheck(MetricRegistry.name(poolName3, CHECK_CATEGORY, CONNECTION_99_PERCENT)));
        /**
         * embedded health check {@link io.bootique.jdbc.instrumented.healthcheck.DataSourceHealthCheck}
         */
        assertTrue(registry.containsHealthCheck("derby3"));

        Map<String, HealthCheckOutcome> results = registry.runHealthChecks();
        assertEquals(results.size(), 6);
    }
}
