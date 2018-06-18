/**
 *  Licensed to ObjectStyle LLC under one
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

package io.bootique.jdbc.tomcat;

import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JdbcTomcatModuleIT {

    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testDataSource() {

        BQRuntime runtime = testFactory.app("-c", "classpath:TomcatJdbcModuleIT_full.yml")
                .autoLoadModules()
                .createRuntime();

        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("derby1");
        assertNotNull(ds);
        assertTrue(ds instanceof org.apache.tomcat.jdbc.pool.DataSource);

        org.apache.tomcat.jdbc.pool.DataSource tomcatDS = (org.apache.tomcat.jdbc.pool.DataSource) ds;

        assertEquals("jdbc:derby:target/derby1;create=true", tomcatDS.getUrl());
        assertEquals("sa", tomcatDS.getUsername());
        assertEquals(0, tomcatDS.getInitialSize());
        assertEquals(1, tomcatDS.getMinIdle());
        assertEquals(3, tomcatDS.getMaxIdle());
        assertEquals(6, tomcatDS.getMaxActive());
    }

    @Test
    public void testDataSource_DriverAutoDetected() throws SQLException {

        BQRuntime runtime = testFactory.app("-c", "classpath:TomcatJdbcModuleIT_nodriver.yml")
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
    public void testDataSource_TypeAutoDetected() {

        BQRuntime runtime = testFactory.app("-c", "classpath:TomcatJdbcModuleIT_notype.yml")
                .autoLoadModules()
                .createRuntime();

        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("derby3");
        assertNotNull(ds);
        assertTrue(ds instanceof org.apache.tomcat.jdbc.pool.DataSource);

        org.apache.tomcat.jdbc.pool.DataSource tomcatDS = (org.apache.tomcat.jdbc.pool.DataSource) ds;

        assertEquals("jdbc:derby:target/derby3;create=true", tomcatDS.getUrl());
    }
}
