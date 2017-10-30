package io.bootique.jdbc;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class LazyDataSourceFactoryIT {

    private static final String URL = "jdbc:derby:target/testdb;create=true";
    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    private Map<String, TomcatDataSourceFactory> configs;
    private TomcatDataSourceFactory derbyConfig;

    @Before
    public void before() {

        this.derbyConfig = new TomcatDataSourceFactory();
        derbyConfig.setUrl(URL);
        derbyConfig.setInitialSize(2);
        derbyConfig.setDriverClassName(DRIVER);

        this.configs = new HashMap<>();
        configs.put("c1", derbyConfig);
    }

    @Test
    public void testCreateDataSource() throws Exception {

        LazyDataSourceFactory factory = new LazyDataSourceFactory(configs);
        ManagedDataSource ds = factory.createDataSource("c1");
        org.apache.tomcat.jdbc.pool.DataSource tomcat = (org.apache.tomcat.jdbc.pool.DataSource) ds.getDataSource();
        assertNotNull(ds);

        try {
            assertEquals(URL, ds.getUrl());
            assertEquals(2, tomcat.getInitialSize());
        } finally {
            tomcat.close();
        }
    }

    @Test
    public void testCreateDataSource_DriverAutoDetected() throws Exception {

        derbyConfig.setDriverClassName(null);

        LazyDataSourceFactory factory = new LazyDataSourceFactory(configs);
        ManagedDataSource ds = factory.createDataSource("c1");
        org.apache.tomcat.jdbc.pool.DataSource tomcat = (org.apache.tomcat.jdbc.pool.DataSource) ds.getDataSource();
        assertNotNull(ds);

        try {
            try (Connection c = tomcat.getConnection()) {
                assertTrue(c.getMetaData().getDriverName().toLowerCase().contains("derby"));
            }
        } finally {
            tomcat.close();
        }
    }

}
