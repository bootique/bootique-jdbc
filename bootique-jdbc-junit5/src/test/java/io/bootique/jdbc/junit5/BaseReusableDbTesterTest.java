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
package io.bootique.jdbc.junit5;

import io.bootique.BQRuntime;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseReusableDbTesterTest extends BaseDbTesterTest {

    protected static final String DB_URL =
            "jdbc:tc:postgresql:11:///mydb" +
                    "?TC_REUSABLE=true" +
                    "&TC_INITFUNCTION=io.bootique.jdbc.junit5.BaseReusableDbTesterTest::initDB";

    @RegisterExtension
    protected static final DbTester db = DbTester
            .testcontainersDb(DB_URL)
            .deleteBeforeEachTest("reusable_a");

    public static void initDB(Connection connection) throws SQLException {
        try (Statement s = connection.createStatement()) {
            s.executeUpdate("create table reusable_a (id integer)");
            s.executeUpdate("insert into reusable_a values (3456)");
        }
    }

    protected void checkEmptyAndInsert(BQRuntime app) {

        run(app, c -> {
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
        });
    }
}
