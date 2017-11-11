package io.bootique.jdbc.tomcat;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TomcatJdbcModuleIT {

    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testCreateDataSource() {

        BQRuntime runtime = testFactory.app("-c", "classpath:TomcatManagedDataSourceFactoryIT_full.yml")
                .autoLoadModules()
                .createRuntime();

        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("derby1");
        assertNotNull(ds);
        assertTrue(ds instanceof org.apache.tomcat.jdbc.pool.DataSource);

        org.apache.tomcat.jdbc.pool.DataSource tomcatDS = (org.apache.tomcat.jdbc.pool.DataSource) ds;

        assertEquals("jdbc:derby:target/derby1;create=true", tomcatDS.getUrl());
        assertEquals(2, tomcatDS.getInitialSize());
    }

    @Test
    public void testCreateDataSource_DriverAutoDetected() throws SQLException {

        BQRuntime runtime = testFactory.app("-c", "classpath:TomcatManagedDataSourceFactoryIT_nodriver.yml")
                .autoLoadModules()
                .createRuntime();

        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("derby2");
        assertNotNull(ds);
        assertTrue(ds instanceof org.apache.tomcat.jdbc.pool.DataSource);


        try (Connection c = ds.getConnection()) {
            assertTrue(c.getMetaData().getDriverName().toLowerCase().contains("derby"));
        }
    }

    @Test
    public void testPartialConfigsNotExcluded() {

        BQRuntime runtime = testFactory.app("-c", "classpath:dummy-3ds.yml").autoLoadModules().createRuntime();
        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);

        Set<String> names = new HashSet<>(factory.allNames());
        assertEquals(new HashSet<>(Arrays.asList("fullds1", "fullds2", "partialds3")), names);
    }

    @Test
    public void testPartialConfigsNotExcluded_Vars() {

        BQRuntime runtime = testFactory.app("-c", "classpath:dummy-2ds.yml")
                .autoLoadModules()
                .module(b -> {
                    BQCoreModule.extend(b)
                            .setVar("BQ_JDBC_PARTIAL_PASSWORD", "p1")
                            .setVar("BQ_JDBC_FULLDS2_PASSWORD", "p2")
                            .setVar("BQ_JDBC_FULLDSVARS_URL", "jdbc:dummy");
                })
                .createRuntime();
        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);

        Set<String> names = new HashSet<>(factory.allNames());
        assertEquals(new HashSet<>(Arrays.asList("fullds1", "fullds2", "PARTIAL", "FULLDSVARS")), names);
    }
}
