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
package io.bootique.jdbc.junit5.derby;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.*;

import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@BQTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DerbyTesterIT extends BaseDerbyTesterTest {

    @BQTestTool
    static final DerbyTester db = DerbyTester.db();

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique.app()
            .autoLoadModules()
            .module(db.moduleWithTestDataSource("myDS"))
            .createRuntime();

    @Test
    @Order(0)
    @DisplayName("Derby DataSource must be in use")
    public void derby() {
        run(app, c -> Assertions.assertEquals("Apache Derby", c.getMetaData().getDatabaseProductName()));
    }

    @Test
    @Order(1)
    @DisplayName("Setup data for subsequent state test")
    public void setupDbState() {
        createDbState(app);
    }

    @Test
    @Order(2)
    @DisplayName("DB state must be preserved between the tests")
    public void dbState() {
        checkDbState(app);
    }

    protected void createDbState(BQRuntime app) {
        run(app, c -> {
            try (Statement s = c.createStatement()) {
                s.executeUpdate("create table a (id integer)");
                s.executeUpdate("insert into a values (345)");
            }
        });
    }

    protected void checkDbState(BQRuntime app) {
        run(app, c -> {
            try (Statement s = c.createStatement()) {
                try (ResultSet rs = s.executeQuery("select * from a")) {
                    assertTrue(rs.next());
                    assertEquals(345, rs.getInt(1));
                }
            }
        });
    }
}
