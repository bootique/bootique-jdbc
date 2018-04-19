package io.bootique.jdbc.instrumented.hikaricp;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariDataSource;
import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.instrumented.hikaricp.metrics.HikariMetricsBridge;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MetricsIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testConnectionStateMetrics() throws SQLException {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/MetricsIT.yml")
                .autoLoadModules()
                .createRuntime();

        MetricRegistry registry = runtime.getInstance(MetricRegistry.class);
        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("db");
        String poolName = ((HikariDataSource) ds).getPoolName();

        // wait for the metrics to be initialized...
        Gauge<Integer> activeConnections = registry.getGauges().get(HikariMetricsBridge.activeConnectionsMetricName(poolName));

        assertNotNull(registry.getGauges().keySet() + "", activeConnections);
        assertEquals(Integer.valueOf(0), activeConnections.getValue());

        for (int i = 0; i < 3; i++) {
            try (Connection c = ds.getConnection()) {
                // the metric does not refresh immediately, so need to test with a delay...
                await("recorded_active_connection")
                        .atMost(1200, TimeUnit.MILLISECONDS)
                        .until(() -> activeConnections.getValue(), equalTo(1));
            }
        }

        await("no_more_active_connections")
                .atMost(1200, TimeUnit.MILLISECONDS).until(() -> activeConnections.getValue(),
                equalTo(0));
    }


    @Test
    public void testUsageMetric() throws SQLException, InterruptedException {

        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/MetricsIT.yml")
                .autoLoadModules()
                .createRuntime();

        MetricRegistry registry = runtime.getInstance(MetricRegistry.class);
        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("db");
        String poolName = ((HikariDataSource) ds).getPoolName();

        Histogram usage = registry.getHistograms().get(HikariMetricsBridge.usageMetricName(poolName));
        assertNotNull(usage);
        assertEquals(0., usage.getSnapshot().get99thPercentile(), 0.001);

        for (int i = 0; i < 10; i++) {
            try (Connection c = ds.getConnection()) {
                // checkout some connections to generate usage stats ... make sure usage is > 0
                Thread.sleep(2);
            }
        }

        await("usage")
                .atMost(1200, TimeUnit.MILLISECONDS)
                .until(() -> usage.getSnapshot().getMax() > 0);
    }

    // TODO: we can't effectively check "wait" metric? Checkout time is normally < 1ms and Hikari uses millisecond clock on MacOS.
}
