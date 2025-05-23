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

package io.bootique.jdbc.junit5.derby.matcher;

import io.bootique.jdbc.junit5.Table;
import io.bootique.jdbc.junit5.derby.DerbyTester;
import io.bootique.jdbc.junit5.matcher.CsvTableMatcher;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;

@BQTest
public class CsvTableMatcherIT {

    @BQTestTool
    static final DerbyTester db = DerbyTester
            .db()
            .deleteBeforeEachTest("t1", "t2", "t3", "t4");

    private static Table T1;
    private static Table T2;
    private static Table T3;
    private static Table T4;

    @BeforeAll
    public static void setupDB() {

        db.execStatement().exec("CREATE TABLE \"t1\" (\"c1\" INT, \"c2\" VARCHAR(10), \"c3\" VARCHAR(10))");
        db.execStatement().exec("CREATE TABLE \"t2\" (\"c1\" INT, \"c2\" INT, \"c3\" DATE, \"c4\" TIMESTAMP)");
        db.execStatement().exec("CREATE TABLE \"t3\" (\"c1\" INT, \"c2\" VARCHAR (10) FOR BIT DATA)");
        db.execStatement().exec("CREATE TABLE \"t4\" (\"c1\" BIGINT, \"c2\" VARCHAR (10), \"c3\" DECIMAL(10,2))");

        T1 = db.getTable("t1");
        T2 = db.getTable("t2");
        T3 = db.getTable("t3");
        T4 = db.getTable("t4");
    }

    @Test
    public void assertMatchesCsv() {

        T1.insertColumns("c1", "c2", "c3")
                .values(2, "tt", "xyz")
                .values(1, "", "abcd")
                .exec();

        new CsvTableMatcher(T1).keyColumns("c1").assertMatches("classpath:io/bootique/jdbc/junit5/derby/matcher/t1_ref.csv");
    }

    @Test
    public void assertMatchesCsv_EmptyAsNulls() {

        T1.insertColumns("c1", "c2", "c3")
                .values(2, "tt", "xyz")
                .values(1, null, "abcd")
                .exec();

        new CsvTableMatcher(T1)
                .keyColumns("c1")
                .emptyStringIsNull()
                .assertMatches("classpath:io/bootique/jdbc/junit5/derby/matcher/t1_ref.csv");
    }

    @Test
    public void assertMatchesCsv_NoMatch() {

        T1.insertColumns("c1", "c2", "c3")
                .values(1, "tt", "xyz")
                .values(2, "", "abcd")
                .exec();

        boolean succeeded;
        try {
            new CsvTableMatcher(T1).keyColumns("c1").assertMatches("classpath:io/bootique/jdbc/junit5/derby/matcher/t1_ref.csv");
            succeeded = true;
        } catch (AssertionError e) {
            // expected
            succeeded = false;
        }

        assertFalse(succeeded, "Must have failed - data sets do not match");
    }

    @Test
    public void assertMatchesCsv_Dates() {


        T2.insertColumns("c1", "c2", "c3", "c4")
                .values(3, null, "2018-01-09", "2018-01-10 14:00:01")
                .values(1, null, "2016-01-09", "2016-01-10 10:00:00")
                .values(2, null, "2017-01-09", "2017-01-10 13:00:01")
                .exec();

        new CsvTableMatcher(T2).keyColumns("c1").assertMatches("classpath:io/bootique/jdbc/junit5/derby/matcher/t2_ref.csv");
    }

    @Test
    public void assertMatchesCsv_Binary() {

        T3.insertColumns("c1", "c2")
                .values(3, null)
                .values(1, "abcd".getBytes())
                .values(2, "kmln".getBytes())
                .exec();

        new CsvTableMatcher(T3).keyColumns("c1").assertMatches("classpath:io/bootique/jdbc/junit5/derby/matcher/t3_ref.csv");
    }

    @Test
    public void assertMatchesCsv_Numbers() {

        T4.insertColumns("c1", "c2", "c3")
                .values(1L, "abc", new BigDecimal("2.34"))
                .values(2L, "xyz", BigDecimal.ZERO)
                .exec();

        new CsvTableMatcher(T4).keyColumns("c1").assertMatches("classpath:io/bootique/jdbc/junit5/derby/matcher/t4_ref.csv");
    }
}
