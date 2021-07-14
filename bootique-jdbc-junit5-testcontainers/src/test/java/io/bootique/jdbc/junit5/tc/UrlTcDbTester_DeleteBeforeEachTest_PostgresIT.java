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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlTcDbTester_DeleteBeforeEachTest_PostgresIT extends BasePostgresTest {

    @RepeatedTest(2)
    @DisplayName("Check tables are clean, insert data for next test")
    public void test() throws SQLException {
        checkNoData();
        insertTestData();
    }

    protected void checkNoData() throws SQLException {
        try (Connection c = db.getConnection()) {
            try (Statement s = c.createStatement()) {
                try (ResultSet rs = s.executeQuery("select count(1) from t3")) {
                    rs.next();
                    assertEquals(0, rs.getInt(1));
                }

                try (ResultSet rs = s.executeQuery("select count(1) from t4")) {
                    rs.next();
                    assertEquals(0, rs.getInt(1));
                }
            }
        }
    }

    protected void insertTestData() throws SQLException {
        try (Connection c = db.getConnection()) {
            try (Statement s = c.createStatement()) {
                s.executeUpdate("insert into t3 (id, name) values (10, 'myname')");
                s.executeUpdate("insert into t4 (id, name, a_id) values (11, 'myname', 10)");
            }
        }
    }
}
