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

import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.BQTestTool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@BQTest
public abstract class BaseGlobalTcTester_PostgresTest {

    private static boolean wasInitialized;

    @BQTestTool(BQTestScope.GLOBAL)
    protected static final TcDbTester db = TcDbTester
            .db("jdbc:tc:postgresql:11:///")
            .initDB(BaseGlobalTcTester_PostgresTest::initDB)
            .deleteBeforeEachTest("reusable_a");

    static void initDB(Connection connection) throws SQLException {

        assertFalse(wasInitialized, "'initDB' is called more than once. Container not reusable?");

        try (Statement s = connection.createStatement()) {
            s.executeUpdate("create table reusable_a (id integer)");
            s.executeUpdate("insert into reusable_a values (3456)");
        }

        wasInitialized = true;
    }

    protected void checkEmptyAndInsert() throws SQLException {

        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try (Statement s = c.createStatement()) {
                try (ResultSet rs = s.executeQuery("select count(1) from reusable_a")) {
                    rs.next();
                    assertEquals(0, rs.getInt(1));
                }
            }

            try (Statement s = c.createStatement()) {
                s.executeUpdate("insert into reusable_a values (1)");
            }

            c.commit();
        }
    }
}
