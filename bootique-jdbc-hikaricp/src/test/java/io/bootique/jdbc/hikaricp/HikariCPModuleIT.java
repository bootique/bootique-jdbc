package io.bootique.jdbc.hikaricp;

import com.zaxxer.hikari.HikariDataSource;
import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HikariCPModuleIT {

    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testDataSource() {

        BQRuntime runtime = testFactory.app("-c", "classpath:HikariCPModuleIT_ds.yml")
                .autoLoadModules()
                .createRuntime();

        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("derby1");
        assertNotNull(ds);
        assertTrue(ds instanceof HikariDataSource);

        HikariDataSource hikariDS = (HikariDataSource) ds;

        assertEquals("jdbc:derby:target/derby1;create=true", hikariDS.getJdbcUrl());
        assertEquals("sa", hikariDS.getUsername());
        assertEquals(1, hikariDS.getMinimumIdle());
        assertEquals(3, hikariDS.getMaximumPoolSize());
    }

    @Test
    public void testDataSource_DriverAutoDetected() throws SQLException {

        BQRuntime runtime = testFactory.app("-c", "classpath:HikariCPModuleIT_nodriver.yml")
                .autoLoadModules()
                .createRuntime();

        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("derby2");
        assertNotNull(ds);
        assertTrue(ds instanceof HikariDataSource);

        try (Connection c = ds.getConnection()) {
            assertTrue(c.getMetaData().getDriverName().toLowerCase().contains("derby"));
        }
    }

    @Test
    public void testDataSource_TypeAutoDetected() {

        BQRuntime runtime = testFactory.app("-c", "classpath:HikariCPModuleIT_notype.yml")
                .autoLoadModules()
                .createRuntime();

        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("derby3");
        assertNotNull(ds);
        assertTrue(ds instanceof HikariDataSource);

        HikariDataSource hikariDS = (HikariDataSource) ds;

        assertEquals("jdbc:derby:target/derby3;create=true", hikariDS.getJdbcUrl());
    }

    // TODO: this functionality will no longer be needed when BQ_ vars support is removed
    @Test
    @Deprecated
    public void testAllNames_PartialConfigsExcluded_Vars() {

        BQRuntime runtime = testFactory.app("-c", "classpath:dummy-2ds.yml")
                .autoLoadModules()
                .module(b -> {
                    BQCoreModule.extend(b)
                            .setVar("BQ_JDBC_PARTIAL_PASSWORD", "p1")
                            .setVar("BQ_JDBC_FULLDS2_PASSWORD", "p2")
                            .setVar("BQ_JDBC_FULLDSVARS_JDBCURL", "jdbc:dummy");
                })
                .createRuntime();
        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);

        Set<String> names = new HashSet<>(factory.allNames());
        assertEquals(new HashSet<>(Arrays.asList("fullds1", "fullds2", "FULLDSVARS")), names);
    }

    @Test
    public void testDataSource_FullConfig() throws SQLException {
        BQRuntime runtime = testFactory.app("-c", "classpath:HikariCPModuleIT_full.yml")
                .autoLoadModules()
                .createRuntime();

        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("ds1");
        assertNotNull(ds);
        assertTrue(ds instanceof HikariDataSource);

        HikariDataSource hikariDS = (HikariDataSource) ds;

        assertEquals(250, hikariDS.getConnectionTimeout());
        assertEquals(250, hikariDS.getValidationTimeout());
        assertEquals(20000, hikariDS.getIdleTimeout());
        assertEquals(20000, hikariDS.getLeakDetectionThreshold());
        assertEquals(50000, hikariDS.getMaxLifetime());
        assertEquals(3, hikariDS.getMaximumPoolSize());
        assertEquals(1, hikariDS.getMinimumIdle());
        assertEquals("x", hikariDS.getUsername());
        assertEquals("x", hikariDS.getPassword());
        assertEquals(100, hikariDS.getInitializationFailTimeout());
        assertEquals("test-catalog", hikariDS.getCatalog());
        assertEquals("org.apache.derby.jdbc.EmbeddedDataSource", hikariDS.getDataSourceClassName());
        assertEquals("jdbc:derby:;", hikariDS.getJdbcUrl());
        assertEquals("test-pool", hikariDS.getPoolName());
        assertEquals("TRANSACTION_SERIALIZABLE", hikariDS.getTransactionIsolation());
        assertTrue(hikariDS.isAutoCommit());
        assertFalse(hikariDS.isReadOnly());
        assertFalse(hikariDS.isIsolateInternalQueries());
        assertFalse(hikariDS.isRegisterMbeans());
        assertFalse(hikariDS.isAllowPoolSuspension());

        try (Connection c = hikariDS.getConnection()) {
            assertEquals("jdbc:derby:target/ds1", c.getMetaData().getURL());
        }
    }

}