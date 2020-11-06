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

import io.bootique.jdbc.junit5.Table;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BQTest
public class Table_MySQLIT {

    @BQTestTool
    static final TcDbTester db = TcDbTester
            .db("jdbc:tc:mysql:8.0.20:///db")
            .initDB("classpath:io/bootique/jdbc/junit5/tc/Table_MySQLIT.sql");

    private static Table T1;

    @BeforeAll
    public static void setupDB() {
        T1 = db.getTable("t1");
    }

    @Test
    public void testDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2018, 1, 10, 4, 0, 1);
        T1.insertColumns("c1", "c3").values(1, ldt).exec();

        T1.matcher().assertMatches(1);

        // comparing timestamps in Java (instead of using a matcher for DB-side comparison) to be able to see the differences
        Object[] data = T1.selectColumns("c3").where("c1", 1).selectOne(null);
        assertEquals(Timestamp.valueOf(ldt), data[0], "Timestamps do not match");
    }
}
