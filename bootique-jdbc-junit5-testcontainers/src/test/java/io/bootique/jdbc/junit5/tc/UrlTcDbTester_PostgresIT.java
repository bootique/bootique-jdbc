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

import io.bootique.jdbc.junit5.tc.unit.BasePostgresTest;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UrlTcDbTester_PostgresIT extends BasePostgresTest {

    @Test
    @Order(0)
    @DisplayName("PostgreSQL DataSource must be in use")
    public void postgres() throws SQLException {
        try (Connection c = db.getConnection()) {
            Assertions.assertEquals("PostgreSQL", c.getMetaData().getDatabaseProductName());
        }
    }

    @Test
    @Order(1)
    @DisplayName("Setup data for subsequent state test")
    public void setupDbState() throws SQLException {
        try (Connection c = db.getConnection()) {
            try (Statement s = c.createStatement()) {
                s.executeUpdate("create table UrlTcDbTester_PostgresIT (id integer)");
                s.executeUpdate("insert into UrlTcDbTester_PostgresIT values (345)");
            }
        }
    }

    @Test
    @Order(2)
    @DisplayName("DB state must be preserved between the tests")
    public void dbState() throws SQLException {
        try (Connection c = db.getConnection()) {
            try (Statement s = c.createStatement()) {
                try (ResultSet rs = s.executeQuery("select * from UrlTcDbTester_PostgresIT")) {
                    assertTrue(rs.next());
                    assertEquals(345, rs.getInt(1));
                }
            }
        }
    }
}
