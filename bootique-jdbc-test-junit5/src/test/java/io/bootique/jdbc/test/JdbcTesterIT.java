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
package io.bootique.jdbc.test;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.test.junit5.BQApp;
import io.bootique.test.junit5.BQTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BQTest
public class JdbcTesterIT {

    static final JdbcTester jdbcTester = new JdbcTester();

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique.app()
            .autoLoadModules()
            .module(jdbcTester.setOrReplaceDataSource("myDS"))
            .createRuntime();

    @Test
    @DisplayName("JdbcTester-provided DataSource must be in use")
    public void testSetOrReplaceDataSource() throws SQLException {

        DataSourceFactory factory = app.getInstance(DataSourceFactory.class);
        assertEquals(Collections.singleton("myDS"), factory.allNames());

        DataSource ds = factory.forName("myDS");
        try (Connection c = ds.getConnection()) {
            try (Statement s = c.createStatement()) {
                s.executeUpdate("create table a (id integer)");
            }
        }
    }
}
