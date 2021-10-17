/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.jdbc.hikaricp;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import com.zaxxer.hikari.util.DriverDataSource;
import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class HikariCPDerbyIT {

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory().autoLoadModules();

    @Test
    public void testDerbyDataSource() throws SQLException {
        BQRuntime runtime = testFactory.app("-c", "classpath:HikariCPDerbyIT.yml").createRuntime();

        DataSource ds4 = runtime.getInstance(DataSourceFactory.class).forName("derby4");
        assertNotNull(ds4);
        assertTrue(ds4 instanceof HikariDataSource);

        HikariDataSource hikariDS = (HikariDataSource) ds4;

        assertEquals("org.apache.derby.jdbc.EmbeddedDataSource", hikariDS.getDataSourceClassName());

        HikariPool pool = (HikariPool) hikariDS.getHikariPoolMXBean();

        assertTrue(pool.getUnwrappedDataSource().getClass().isAssignableFrom(EmbeddedDataSource.class));

        try (Connection c = hikariDS.getConnection()) {
            assertEquals("jdbc:derby:target/derby4", c.getMetaData().getURL());
        }
    }

    @Test
    public void testDerbyDriverDataSource() throws SQLException {
        BQRuntime runtime = testFactory.app("-c", "classpath:HikariCPDerbyIT.yml").createRuntime();

        DataSource ds5 = runtime.getInstance(DataSourceFactory.class).forName("derby5");
        assertNotNull(ds5);
        assertTrue(ds5 instanceof HikariDataSource);

        HikariDataSource hikariDS = (HikariDataSource) ds5;

        assertEquals("org.apache.derby.jdbc.EmbeddedDriver", hikariDS.getDriverClassName());

        HikariPool pool = (HikariPool) hikariDS.getHikariPoolMXBean();

        assertTrue(pool.getUnwrappedDataSource() instanceof DriverDataSource);

        try (Connection c = hikariDS.getConnection()) {
            assertEquals("jdbc:derby:", c.getMetaData().getURL());
        }
    }

    @Test
    public void testDerbyDataSource_ConnectionAttributes() throws SQLException {
        BQRuntime runtime = testFactory.app("-c", "classpath:HikariCPDerbyIT_connAttrs.yml").createRuntime();

        DataSource ds6 = runtime.getInstance(DataSourceFactory.class).forName("derby6");
        assertNotNull(ds6);

        HikariDataSource hikariDS = (HikariDataSource) ds6;

        try (Connection c = hikariDS.getConnection()) {
            assertEquals("jdbc:derby:", c.getMetaData().getURL());
        }
    }

    @Test
    public void testDerbyDriver_ConnectionAttributes() throws SQLException {
        BQRuntime runtime = testFactory.app("-c", "classpath:HikariCPDerbyIT_connAttrs.yml").createRuntime();

        DataSource ds7 = runtime.getInstance(DataSourceFactory.class).forName("derby7");
        assertNotNull(ds7);

        HikariDataSource hikariDS = (HikariDataSource) ds7;

        try (Connection c = hikariDS.getConnection()) {
            assertEquals("jdbc:derby:target/derby7", c.getMetaData().getURL());
        }
    }
}
