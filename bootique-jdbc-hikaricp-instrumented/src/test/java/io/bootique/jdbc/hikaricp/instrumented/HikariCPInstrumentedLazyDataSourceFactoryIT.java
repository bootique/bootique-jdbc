package io.bootique.jdbc.hikaricp.instrumented;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariDataSource;
import io.bootique.BQRuntime;
import io.bootique.jdbc.hikaricp.HikariCPDataSourceFactory;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HikariCPInstrumentedLazyDataSourceFactoryIT {
    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    private static final String URL = "jdbc:derby:target/testdb;create=true";
    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    private Map<String, HikariCPDataSourceFactory> configs;
    private HikariCPDataSourceFactory derbyConfig;

    @Before
    public void before() {

        this.derbyConfig = new HikariCPDataSourceFactory();
        derbyConfig.setJdbcUrl(URL);
        derbyConfig.setMaxPoolSize(2);
        derbyConfig.setDriverClassName(DRIVER);
        derbyConfig.setMinIdle(1);

        this.configs = new HashMap<>();
        configs.put("c1", derbyConfig);
    }

    @Test
    public void testCreateDataSource() throws Exception {

        BQRuntime runtime = testFactory.app("-c", "classpath:dummy-1ds.yml").autoLoadModules().createRuntime();
        MetricRegistry registry = runtime.getInstance(MetricRegistry.class);

        HikariCPInstrumentedLazyDataSourceFactory factory = new HikariCPInstrumentedLazyDataSourceFactory(configs, registry);
        HikariDataSource ds = factory.createDataSource("c1");

        assertNotNull(ds);

        try {
            assertEquals(URL, ds.getJdbcUrl());
            assertEquals(2, ds.getMaximumPoolSize());
            assertEquals(1, ds.getMinimumIdle());
        } finally {
            ds.close();
        }
    }

}
