package io.bootique.jdbc.tomcat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class TomcatCPLazyDataSourceFactoryIT {

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

        TomcatCPLazyDataSourceFactory factory = new TomcatCPLazyDataSourceFactory(configs);
        org.apache.tomcat.jdbc.pool.DataSource ds = factory.createDataSource("c1");
        Assert.assertNotNull(ds);

        try {
            Assert.assertEquals(URL, ds.getUrl());
            Assert.assertEquals(2, ds.getInitialSize());
        } finally {
            ds.close();
        }
    }

    @Test
    public void testCreateDataSource_DriverAutoDetected() throws Exception {

        derbyConfig.setDriverClassName(null);

        TomcatCPLazyDataSourceFactory factory = new TomcatCPLazyDataSourceFactory(configs);
        org.apache.tomcat.jdbc.pool.DataSource ds = factory.createDataSource("c1");
        Assert.assertNotNull(ds);

        try {
            try (Connection c = ds.getConnection()) {
                Assert.assertTrue(c.getMetaData().getDriverName().toLowerCase().contains("derby"));
            }
        } finally {
            ds.close();
        }
    }

}
