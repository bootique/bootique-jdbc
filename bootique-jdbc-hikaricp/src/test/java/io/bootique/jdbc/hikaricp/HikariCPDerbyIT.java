package io.bootique.jdbc.hikaricp;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import com.zaxxer.hikari.util.DriverDataSource;
import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.test.junit.BQTestFactory;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.Rule;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HikariCPDerbyIT {

    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testDerbyDataSource() throws SQLException {
        BQRuntime runtime = testFactory.app("-c", "classpath:derby-ds1.yml")
                .autoLoadModules()
                .createRuntime();

        DataSource ds1 = runtime.getInstance(DataSourceFactory.class).forName("derby1");
        assertNotNull(ds1);
        assertTrue(ds1 instanceof HikariDataSource);

        HikariDataSource hikariDS = (HikariDataSource) ds1;

        assertEquals("org.apache.derby.jdbc.EmbeddedDataSource", hikariDS.getDataSourceClassName());

        HikariPool pool = (HikariPool) hikariDS.getHikariPoolMXBean();

        assertTrue(pool.getUnwrappedDataSource().getClass().isAssignableFrom(EmbeddedDataSource.class));

        try (Connection c = hikariDS.getConnection()) {
            assertEquals("jdbc:derby:target/derby1", c.getMetaData().getURL());
        }
    }

    @Test
    public void testDerbyDriverDataSource() throws SQLException {
        BQRuntime runtime = testFactory.app("-c", "classpath:derby-ds2.yml")
                .autoLoadModules()
                .createRuntime();

        DataSource ds2 = runtime.getInstance(DataSourceFactory.class).forName("derby2");
        assertNotNull(ds2);
        assertTrue(ds2 instanceof HikariDataSource);

        HikariDataSource hikariDS = (HikariDataSource) ds2;

        assertEquals("org.apache.derby.jdbc.EmbeddedDriver", hikariDS.getDriverClassName());

        HikariPool pool = (HikariPool) hikariDS.getHikariPoolMXBean();

        assertTrue(pool.getUnwrappedDataSource() instanceof DriverDataSource);

        try (Connection c = hikariDS.getConnection()) {
            assertEquals("jdbc:derby:", c.getMetaData().getURL());
        }
    }

    @Test
    public void testDerbyDataSource_ConnectionAttributes() throws SQLException {
        BQRuntime runtime = testFactory.app("-c", "classpath:derby-ds-attrs.yml")
                .autoLoadModules()
                .createRuntime();

        DataSource ds3 = runtime.getInstance(DataSourceFactory.class).forName("derby3");
        assertNotNull(ds3);

        HikariDataSource hikariDS = (HikariDataSource) ds3;

        try (Connection c = hikariDS.getConnection()) {
            assertEquals("jdbc:derby:", c.getMetaData().getURL());
        }
    }

    @Test
    public void testDerbyDriver_ConnectionAttributes() throws SQLException {
        BQRuntime runtime = testFactory.app("-c", "classpath:derby-ds-attrs.yml")
                .autoLoadModules()
                .createRuntime();

        DataSource ds4 = runtime.getInstance(DataSourceFactory.class).forName("derby4");
        assertNotNull(ds4);

        HikariDataSource hikariDS = (HikariDataSource) ds4;

        try (Connection c = hikariDS.getConnection()) {
            assertEquals("jdbc:derby:target/derby4", c.getMetaData().getURL());
        }
    }
}
