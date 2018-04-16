package io.bootique.jdbc.instrumented.hikaricp;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariDataSource;
import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HikariCPInstrumentedModule_MetricsIT {

    static final String METRIC_CATEGORY = "pool";
    static final String METRIC_NAME_WAIT = "Wait";
    static final String METRIC_NAME_TOTAL_CONNECTIONS = "TotalConnections";
    static final String METRIC_NAME_IDLE_CONNECTIONS = "IdleConnections";
    static final String METRIC_NAME_ACTIVE_CONNECTIONS = "ActiveConnections";
    static final String METRIC_NAME_PENDING_CONNECTIONS = "PendingConnections";

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testMetrics() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/HikariCPInstrumentedModule_MetricsIT.yml")
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
}