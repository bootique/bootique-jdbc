package io.bootique.jdbc.hikaricp;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class HikariCPLazyDataSourceFactoryIT {

    private static final String URL = "jdbc:derby:target/testdb;create=true";
    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    private Map<String, HikariCPDataSourceFactory> configs;
    private HikariCPDataSourceFactory derbyConfig;

    @Before
    public void before() {

        this.derbyConfig = new HikariCPDataSourceFactory();
        derbyConfig.setJdbcUrl(URL);
        derbyConfig.setMaxPoolSize(2);
        derbyConfig.setMinIdle(1);
        derbyConfig.setDriverClassName(DRIVER);

        this.configs = new HashMap<>();
        configs.put("c1", derbyConfig);
    }

    @Test
    public void testCreateDataSource() throws Exception {

        HikariCPLazyDataSourceFactory factory = new HikariCPLazyDataSourceFactory(configs);
        HikariDataSource ds = factory.createDataSource("c1");
        Assert.assertNotNull(ds);

        try {
            Assert.assertEquals(URL, ds.getJdbcUrl());
            Assert.assertEquals(2, ds.getMaximumPoolSize());
        } finally {
            ds.close();
        }
    }

}
