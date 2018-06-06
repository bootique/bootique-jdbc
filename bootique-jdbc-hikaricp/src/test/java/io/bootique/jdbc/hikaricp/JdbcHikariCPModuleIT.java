/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.jdbc.hikaricp;

import com.zaxxer.hikari.HikariDataSource;
import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JdbcHikariCPModuleIT {

    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testDataSource() {

        BQRuntime runtime = testFactory.app("-c", "classpath:HikariCPModuleIT_ds.yml")
                .autoLoadModules()
                .createRuntime();

        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("derby");
        assertNotNull(ds);
        assertTrue(ds instanceof HikariDataSource);

        HikariDataSource hikariDS = (HikariDataSource) ds;

        assertEquals("jdbc:derby:target/HikariCPModuleIT_ds;create=true", hikariDS.getJdbcUrl());
        assertEquals("sa", hikariDS.getUsername());
        assertEquals(1, hikariDS.getMinimumIdle());
        assertEquals(3, hikariDS.getMaximumPoolSize());

        assertEquals("Hikari pool name must be the same as Bootique DataSource name",
                "derby", hikariDS.getPoolName());
    }

    @Test
    public void testDataSource_DriverAutoDetected() throws SQLException {

        BQRuntime runtime = testFactory.app("-c", "classpath:HikariCPModuleIT_nodriver.yml")
                .autoLoadModules()
                .createRuntime();

        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("derby");
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

        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("derby");
        assertNotNull(ds);
        assertTrue(ds instanceof HikariDataSource);

        HikariDataSource hikariDS = (HikariDataSource) ds;

        assertEquals("jdbc:derby:target/HikariCPModuleIT_notype;create=true", hikariDS.getJdbcUrl());
    }

    @Test
    public void testDataSource_FullConfig() throws SQLException {
        BQRuntime runtime = testFactory.app("-c", "classpath:HikariCPModuleIT_full.yml")
                .autoLoadModules()
                .createRuntime();

        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("derby");
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
        assertEquals("jdbc:derby:target/HikariCPModuleIT_full;", hikariDS.getJdbcUrl());
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
