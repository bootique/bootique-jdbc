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

package io.bootique.jdbc.test.derby;

import io.bootique.BQRuntime;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.Table;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;


public class DerbyDatabaseIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    private File derbyDir;
    private DatabaseChannel channel;

    @Before
    public void before() {

        this.derbyDir = new File("target/derby/DerbyDatabaseIT");

        BQRuntime runtime = testFactory
                .app("--config=classpath:io/bootique/jdbc/test/DerbyDatabaseIT.yml")
                .autoLoadModules()
                .createRuntime();
        this.channel = DatabaseChannel.get(runtime);
        assertNotNull(this.channel);
    }

    @Test
    public void dbOp1() {

        channel.execStatement().append("CREATE TABLE A (ID int)").exec();

        assertTrue(derbyDir.exists());

        Table a = channel.newTable("A").columnNames("ID").build();

        a.matcher().assertNoMatches();
        a.insert(5).insert(6);
        a.matcher().assertMatches(2);
    }

    @Test
    public void dbOp2() {

        // second test, to ensure the DB was cleaned up...

        channel.execStatement().append("CREATE TABLE B (ID int)").exec();

        assertTrue(derbyDir.exists());

        Table b = channel.newTable("B").columnNames("ID").build();

        b.matcher().assertNoMatches();
        b.insert(5).insert(6);
        b.matcher().assertMatches(2);
    }
}
