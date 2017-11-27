package io.bootique.jdbc.instrumented.hikaricp;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.DataSourceListener;
import io.bootique.test.junit.BQTestFactory;
import org.junit.ClassRule;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
    public void testMetrics() {
        BQRuntime runtime = TEST_FACTORY.app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/dummy-ds.yml")
                .autoLoadModules()
                .createRuntime();

        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);

        DataSource dataSource = factory.forName("DerbyDatabaseIT");
        assertNotNull(dataSource);

        MetricRegistry metricRegistry = runtime.getInstance(MetricRegistry.class);
        assertNotEquals(metricRegistry.getGauges().size(), 0);
    }
}
