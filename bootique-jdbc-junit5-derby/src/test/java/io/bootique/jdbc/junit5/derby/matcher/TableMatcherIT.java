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
import io.bootique.jdbc.junit5.matcher.TableMatcher;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class TableMatcherIT {

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
    public void assertMatches() {
        TableMatcher matcher = new TableMatcher(T1);
        matcher.assertMatches(0);

        T1.insert(1, "y", "z");
        matcher.assertMatches(1);

        T1.insert(2, "a", "b");
        matcher.assertMatches(2);
    }

    @Test
    public void assertMatches_Eq() {
        TableMatcher matcher = new TableMatcher(T1);

        T1.insert(1, "y", "z");
        T1.insert(2, "a", "b");
        matcher.eq("c3", "z").andEq("c1", 1).assertMatches(1);
    }

    @Test
    public void assertMatches_Eq_Null() {
        TableMatcher matcher = new TableMatcher(T1);

        T1.insert(1, "y", "z");
        T1.insert(2, "a", null);
        matcher.eq("c3", null).andEq("c1", 2).assertMatches(1);
    }

    @Test
    public void assertMatches_Eq_Decimal() {
        TableMatcher matcher = new TableMatcher(T4);

        T4.insertColumns("c1", "c2", "c3")
                .values(1L, "abc", new BigDecimal("2.34"))
                .exec();

        matcher.eq("c3", new BigDecimal("2.34")).andEq("c2", "abc").andEq("c1", 1L).assertMatches(1);
    }

    @Test
    public void assertMatches_In() {
        TableMatcher matcher = new TableMatcher(T1);

        T1.insert(1, "y", "z");
        T1.insert(2, "a", "b");
        T1.insert(3, "c", "d");

        matcher.in("c3", "z", "d").assertMatches(2);
    }

    // TODO: no null support in IN yet
    @Test
    public void assertMatches_In_Null() {
        TableMatcher matcher = new TableMatcher(T1);

        T1.insert(1, "y", "z");
        T1.insert(2, "a", null);
        T1.insert(3, "c", "d");

        assertThrows(IllegalArgumentException.class, () -> matcher.in("c3", "z", null).assertMatches(2));
    }

    @Test
    public void assertMatches_In_Eq() {
        TableMatcher matcher = new TableMatcher(T1);

        T1.insert(1, "y", "z");
        T1.insert(2, "a", "x");
        T1.insert(3, "c", "d");

        matcher.in("c3", "z", "x").andEq("c2", "a").assertMatches(1);
    }

    @Test
    public void assertMatches_Negative() {
        TableMatcher matcher = new TableMatcher(T1);
        assertAssertionError(() -> matcher.assertMatches(1), "The matcher incorrectly assumed there's 1 row in " +
                "DB when there's none.");
    }

    @Test
    public void assertMatches_Eq_Negative() {
        TableMatcher matcher = new TableMatcher(T1);

        T1.insert(1, "y", "z");

        assertAssertionError(() -> matcher.eq("c2", "z").andEq("c1", 2).assertMatches(1),
                "The matcher incorrectly assumed there was 1 row matching condition.");
    }

    @Deprecated
    @Test
    public void assertMatchesCsv() {
        TableMatcher matcher = new TableMatcher(T1);

        T1.insertColumns("c1", "c2", "c3")
                .values(2, "tt", "xyz")
                .values(1, "", "abcd")
                .exec();

        matcher.assertMatchesCsv("classpath:io/bootique/jdbc/junit5/derby/matcher/t1_ref.csv", "c1");
    }

    @Deprecated
    @Test
    public void assertMatchesCsv_NoMatch() {

        TableMatcher matcher = new TableMatcher(T1);

        T1.insertColumns("c1", "c2", "c3")
                .values(1, "tt", "xyz")
                .values(2, "", "abcd")
                .exec();

        boolean succeeded;
        try {
            matcher.assertMatchesCsv("classpath:io/bootique/jdbc/junit5/derby/matcher/t1_ref.csv", "c1");
            succeeded = true;
        } catch (AssertionError e) {
            // expected
            succeeded = false;
        }

        assertFalse(succeeded, "Must have failed - data sets do not match");
    }

    @Deprecated
    @Test
    public void assertMatchesCsv_Dates() {

        TableMatcher matcher = new TableMatcher(T2);

        T2.insertColumns("c1", "c2", "c3", "c4")
                .values(3, null, "2018-01-09", "2018-01-10 14:00:01")
                .values(1, null, "2016-01-09", "2016-01-10 10:00:00")
                .values(2, null, "2017-01-09", "2017-01-10 13:00:01")
                .exec();

        matcher.assertMatchesCsv("classpath:io/bootique/jdbc/junit5/derby/matcher/t2_ref.csv", "c1");
    }

    @Deprecated
    @Test
    public void assertMatchesCsv_Binary() {

        TableMatcher matcher = new TableMatcher(T3);

        T3.insertColumns("c1", "c2")
                .values(3, null)
                .values(1, "abcd".getBytes())
                .values(2, "kmln".getBytes())
                .exec();

        matcher.assertMatchesCsv("classpath:io/bootique/jdbc/junit5/derby/matcher/t3_ref.csv", "c1");
    }

    @Deprecated
    @Test
    public void assertMatchesCsv_Numbers() {

        TableMatcher matcher = new TableMatcher(T4);

        T4.insertColumns("c1", "c2", "c3")
                .values(1L, "abc", new BigDecimal("2.34"))
                .values(2L, "xyz", BigDecimal.ZERO)
                .exec();

        matcher.assertMatchesCsv("classpath:io/bootique/jdbc/junit5/derby/matcher/t4_ref.csv", "c1");
    }

    private void assertAssertionError(Runnable test, String whenNoErrorMessage) {
        try {
            test.run();
        } catch (AssertionError e) {
            // expected...
            return;
        }

        fail("Unexpected assertion success: " + whenNoErrorMessage);
    }
}
