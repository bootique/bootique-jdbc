package io.bootique.jdbc.instrumented.hikaricp;

import com.codahale.metrics.MetricRegistry;
import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class HikariCPMetricsInitializerIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testMetrics() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/HikariCPMetricsInitializer.yml")
                .autoLoadModules()
                .createRuntime();

        MetricRegistry metricRegistry = runtime.getInstance(MetricRegistry.class);

        // fault DataSource to init metrics
        runtime.getInstance(DataSourceFactory.class).forName("db");

        Set<String> expectedTimers = new HashSet<>(asList("bq.JdbcHikariCP.Pool.db.Wait"));
        assertEquals(expectedTimers, metricRegistry.getTimers().keySet());

        Set<String> expectedGauges = new HashSet<>(asList(
                "bq.JdbcHikariCP.Pool.db.ActiveConnections",
                "bq.JdbcHikariCP.Pool.db.IdleConnections",
                "bq.JdbcHikariCP.Pool.db.PendingConnections",
                "bq.JdbcHikariCP.Pool.db.TotalConnections"));

        assertEquals(expectedGauges, metricRegistry.getGauges().keySet());
    }
}