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
import io.bootique.Bootique;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@BQTest
public class DbTester_setOrReplaceDataSourceIT {

    @RegisterExtension
    static final DbTester db = DbTester.derbyDb();

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique.app()
            .autoLoadModules()
            .module(db.moduleWithTestDataSource("db1"))
            .module(db.moduleWithTestDataSource("db2"))
            .createRuntime();

    @Test
    public void testTwoDataSources() {
        DataSourceFactory dsf = app.getInstance(DataSourceFactory.class);
        assertEquals(new HashSet<>(asList("db1", "db2")), dsf.allNames());
    }
}
