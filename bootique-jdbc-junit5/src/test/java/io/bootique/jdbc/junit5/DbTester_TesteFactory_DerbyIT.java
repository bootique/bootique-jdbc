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
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.junit5.BQTestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class DbTester_TesteFactory_DerbyIT {

    @RegisterExtension
    static final DbTester db = DbTester.derbyDb();

    @RegisterExtension
    final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testBootstrapWithTestFactory() {
        BQRuntime rt1 = testFactory
                .app()
                .autoLoadModules()
                .module(db.moduleWithTestDataSource("ds"))
                .createRuntime();

        DataSource ds1 = rt1.getInstance(DataSourceFactory.class).forName("ds");
        assertNotNull(ds1);

        BQRuntime rt2 = testFactory
                .app()
                .autoLoadModules()
                .module(db.moduleWithTestDataSource("ds"))
                .createRuntime();

        DataSource ds2 = rt2.getInstance(DataSourceFactory.class).forName("ds");
        assertNotNull(ds2);
        assertSame(ds1, ds2);
    }
}
