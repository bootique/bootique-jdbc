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
package io.bootique.jdbc.junit5.tc;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@BQTest
public class TcTester_InitDB_MySQL_Container_RootIT extends BaseTcTesterTest {

    @Container
    static final MySQLContainer db = new MySQLContainer("mysql:8.0.20")
            .withDatabaseName("xdb")
            .withUsername("mysqluser")
            .withPassword("secret");

    @BQTestTool
    static final TcTester dbTester = TcTester
            // Non-root password is also forced to be the root password
            .db(db, "root", "secret")
            .initDB("classpath:io/bootique/jdbc/junit5/tc/TcTester_InitDB_MySQLIT.sql");

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique.app()
            .autoLoadModules()
            .module(dbTester.moduleWithTestDataSource("myDS"))
            .createRuntime();

    @Test
    @DisplayName("Explicit container, root user - DB was initialized")
    public void testInitDB() {
        run(app, c -> {
            try (Statement s = c.createStatement()) {
                try (ResultSet rs = s.executeQuery("select * from b")) {
                    assertTrue(rs.next());
                    assertEquals(12, rs.getInt("id"));
                    assertEquals("myname", rs.getString("name"));
                }
            }
        });
    }
}
