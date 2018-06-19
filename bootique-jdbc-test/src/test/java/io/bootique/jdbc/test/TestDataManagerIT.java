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
import io.bootique.test.junit.BQTestFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class TestDataManagerIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();
    private static Table T1;
    private static Table T2;

    @Rule
    public TestDataManager dataManager = new TestDataManager(true, T1, T2);

    @BeforeClass
    public static void setupDB() {
        BQRuntime testRuntime = TEST_FACTORY
                .app("--config=classpath:io/bootique/jdbc/test/TestDataManagerIT.yml")
                .autoLoadModules()
                .createRuntime();

        DatabaseChannel channel = DatabaseChannel.get(testRuntime);

        channel.execStatement().exec("CREATE TABLE \"t1\" (\"id\" INT NOT NULL PRIMARY KEY, \"name\" VARCHAR(10))");
        channel.execStatement().exec("CREATE TABLE \"t2\" (\"id\" INT NOT NULL PRIMARY KEY, \"name\" VARCHAR(10), \"t1_id\" INT)");

        T1 = channel.newTable("t1").columnNames("id", "name").build();
        T2 = channel.newTable("t2").columnNames("id", "name", "t1_id").build();
    }

    @Test
    public void test1() {

        T1.matcher().assertNoMatches();
        T2.matcher().assertNoMatches();

        T1.insert(1, "x");
        T2.insert(1, "x1", 1);

        T1.matcher().assertOneMatch();
        T2.matcher().assertOneMatch();
    }

    @Test
    public void test2() {

        T1.matcher().assertNoMatches();
        T2.matcher().assertNoMatches();

        T1.insert(2, "x");
        T2.insert(2, "x2", 2);

        T1.matcher().assertOneMatch();
        T2.matcher().assertOneMatch();
    }

}
