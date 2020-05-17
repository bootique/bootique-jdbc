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
import io.bootique.test.junit5.BQApp;
import io.bootique.test.junit5.BQTest;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BQTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JdbcTester_Testcontainers_PostgresIT extends BaseJdbcTesterTest {

    static final JdbcTester jdbcTester = new JdbcTester()
            .useTestcontainers("jdbc:tc:postgresql:11:///mydb");

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique.app()
            .autoLoadModules()
            .module(jdbcTester.setOrReplaceDataSource("myDS"))
            .createRuntime();

    @Test
    @Order(0)
    @DisplayName("PostgreSQL DataSource must be in use")
    public void testPostgres() {
        run(app, c -> assertEquals("PostgreSQL", c.getMetaData().getDatabaseProductName()));
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
    public void testDbState() {
        checkDbState(app);
    }
}
