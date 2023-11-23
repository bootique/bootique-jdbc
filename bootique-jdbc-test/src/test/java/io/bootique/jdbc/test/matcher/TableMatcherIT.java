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

package io.bootique.jdbc.test.matcher;

import io.bootique.BQRuntime;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.Table;
import io.bootique.jdbc.test.TestDataManager;
import io.bootique.test.junit.BQTestFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class TableMatcherIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();
    private static Table T1;
    private static Table T2;
    private static Table T3;
    private static Table T4;

    @Rule
    public TestDataManager dataManager = new TestDataManager(true, T1);

    @BeforeClass
    public static void setupDB() {
        BQRuntime runtime = TEST_FACTORY
                .app("-c", "classpath:io/bootique/jdbc/test/matcher/TableMatcherIT.yml")
                .autoLoadModules()
                .createRuntime();

        DatabaseChannel channel = DatabaseChannel.get(runtime);

        channel.execStatement().exec("CREATE TABLE \"t1\" (\"c1\" INT, \"c2\" VARCHAR(10), \"c3\" VARCHAR(10))");
        channel.execStatement().exec("CREATE TABLE \"t2\" (\"c1\" INT, \"c2\" INT, \"c3\" DATE, \"c4\" TIMESTAMP)");
        channel.execStatement().exec("CREATE TABLE \"t3\" (\"c1\" INT, \"c2\" VARCHAR (10) FOR BIT DATA)");
        channel.execStatement().exec("CREATE TABLE \"t4\" (\"c1\" BIGINT, \"c2\" VARCHAR (10))");

        T1 = channel.newTable("t1").columnNames("c1", "c2", "c3").initColumnTypesFromDBMetadata().build();
        T2 = channel.newTable("t2").columnNames("c1", "c2", "c3", "c4").initColumnTypesFromDBMetadata().build();
        T3 = channel.newTable("t3").columnNames("c1", "c2").initColumnTypesFromDBMetadata().build();
        T4 = channel.newTable("t4").columnNames("c1", "c2").initColumnTypesFromDBMetadata().build();
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
        matcher.eq("c3", "z").eq("c1", 1).assertMatches(1);
    }

    @Test
    public void assertMatches_Eq_Null() {
        TableMatcher matcher = new TableMatcher(T1);

        T1.insert(1, "y", "z");
        T1.insert(2, "a", null);
        matcher.eq("c3", null).eq("c1", 2).assertMatches(1);
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
    @Test(expected = IllegalArgumentException.class)
    public void assertMatches_In_Null() {
        TableMatcher matcher = new TableMatcher(T1);

        T1.insert(1, "y", "z");
        T1.insert(2, "a", null);
        T1.insert(3, "c", "d");

        matcher.in("c3", "z", null).assertMatches(2);
    }

    @Test
    public void assertMatches_In_Eq() {
        TableMatcher matcher = new TableMatcher(T1);

        T1.insert(1, "y", "z");
        T1.insert(2, "a", "x");
        T1.insert(3, "c", "d");

        matcher.in("c3", "z", "x").eq("c2", "a").assertMatches(1);
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

        assertAssertionError(() -> matcher.eq("c2", "z").eq("c1", 2).assertMatches(1),
                "The matcher incorrectly assumed there was 1 row matching condition.");
    }

    @Test
    public void assertMatchesCsv() {
        TableMatcher matcher = new TableMatcher(T1);

        T1.insertColumns("c1", "c2", "c3")
                .values(2, "tt", "xyz")
                .values(1, "", "abcd")
                .exec();

        matcher.assertMatchesCsv("classpath:io/bootique/jdbc/test/matcher/t1_ref.csv", "c1");
    }

    @Test
    public void assertMatchesCsv_NoMatch() {

        TableMatcher matcher = new TableMatcher(T1);

        T1.insertColumns("c1", "c2", "c3")
                .values(1, "tt", "xyz")
                .values(2, "", "abcd")
                .exec();

        boolean succeeded;
        try {
            matcher.assertMatchesCsv("classpath:io/bootique/jdbc/test/matcher/t1_ref.csv", "c1");
            succeeded = true;
        } catch (AssertionError e) {
            // expected
            succeeded = false;
        }

        assertFalse("Must have failed - data sets do not match", succeeded);
    }

    @Test
    public void assertMatchesCsv_Dates() {

        TableMatcher matcher = new TableMatcher(T2);

        T2.insertColumns("c1", "c2", "c3", "c4")
                .values(3, null, "2018-01-09", "2018-01-10 14:00:01")
                .values(1, null, "2016-01-09", "2016-01-10 10:00:00")
                .values(2, null, "2017-01-09", "2017-01-10 13:00:01")
                .exec();

        matcher.assertMatchesCsv("classpath:io/bootique/jdbc/test/matcher/t2_ref.csv", "c1");
    }

    @Test
    public void assertMatchesCsv_Binary() {

        TableMatcher matcher = new TableMatcher(T3);

        T3.insertColumns("c1", "c2")
                .values(3, null)
                .values(1, "abcd".getBytes())
                .values(2, "kmln".getBytes())
                .exec();

        matcher.assertMatchesCsv("classpath:io/bootique/jdbc/test/matcher/t3_ref.csv", "c1");
    }

    @Test
    public void assertMatchesCsv_Bigint() {

        TableMatcher matcher = new TableMatcher(T4);

        T4.insertColumns("c1", "c2")
                .values(1L, "abc")
                .values(2L, "xyz")
                .exec();

        matcher.assertMatchesCsv("classpath:io/bootique/jdbc/test/matcher/t4_ref.csv", "c1");
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
