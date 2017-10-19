package io.bootique.jdbc.tomcat.instrumented;

import com.codahale.metrics.MetricRegistry;
import io.bootique.BQRuntime;
import io.bootique.jdbc.tomcat.TomcatCPDataSourceFactory;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TomcatCPInstrumentedLazyDataSourceFactoryIT {

    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    private static final String URL = "jdbc:derby:target/testdb;create=true";
    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    private Map<String, TomcatCPDataSourceFactory> configs;
    private TomcatCPDataSourceFactory derbyConfig;

    @Before
    public void before() {

        this.derbyConfig = new TomcatCPDataSourceFactory();
        derbyConfig.setUrl(URL);
        derbyConfig.setInitialSize(2);
        derbyConfig.setDriverClassName(DRIVER);

        this.configs = new HashMap<>();
        configs.put("c1", derbyConfig);
    }

    @Test
    public void testCreateDataSource() throws Exception {

        BQRuntime runtime = testFactory.app("-c", "classpath:dummy-1ds.yml").autoLoadModules().createRuntime();
        MetricRegistry registry = runtime.getInstance(MetricRegistry.class);

        TomcatCPInstrumentedLazyDataSourceFactory factory = new TomcatCPInstrumentedLazyDataSourceFactory(configs, registry);
        org.apache.tomcat.jdbc.pool.DataSource ds = factory.createDataSource("c1");

        assertNotNull(ds);

        try {
            assertEquals(URL, ds.getUrl());
            assertEquals(2, ds.getInitialSize());
        } finally {
            ds.close();
        }
    }

    @Test
    public void testCreateDataSource_DriverAutoDetected() throws Exception {

        derbyConfig.setDriverClassName(null);

        BQRuntime runtime = testFactory.app("-c", "classpath:dummy-1ds.yml").autoLoadModules().createRuntime();
        MetricRegistry registry = runtime.getInstance(MetricRegistry.class);

        TomcatCPInstrumentedLazyDataSourceFactory factory = new TomcatCPInstrumentedLazyDataSourceFactory(configs, registry);
        org.apache.tomcat.jdbc.pool.DataSource ds = factory.createDataSource("c1");

        assertNotNull(ds);

        try {
            try (Connection c = ds.getConnection()) {
                assertTrue(c.getMetaData().getDriverName().toLowerCase().contains("derby"));
            }
        } finally {
            ds.close();
        }
    }
}
