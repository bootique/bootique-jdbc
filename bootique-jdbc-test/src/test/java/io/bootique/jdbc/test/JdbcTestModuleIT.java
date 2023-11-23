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
import io.bootique.di.Binder;
import io.bootique.di.Key;
import io.bootique.di.BQModule;
import io.bootique.di.Provides;
import io.bootique.di.TypeLiteral;
import io.bootique.jdbc.DataSourceListener;
import io.bootique.jdbc.JdbcModule;
import io.bootique.log.BootLogger;
import io.bootique.test.junit.BQTestFactory;
import org.junit.ClassRule;
import org.junit.Test;

import javax.inject.Singleton;
import javax.sql.DataSource;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class JdbcTestModuleIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();

    @Test
    public void listenersClasses_Injected() {
        BQRuntime runtime = TEST_FACTORY.app("-c", "classpath:io/bootique/jdbc/test/dummy-ds.yml")
                .autoLoadModules()
                .module(binder -> {
                    JdbcModule.extend(binder)
                            .addDataSourceListener(new TestDataSourceListener1())
                            .addDataSourceListener(new TestDataSourceListener2());
                }).createRuntime();


        TypeLiteral<Set<DataSourceListener>> typeLiteral = new TypeLiteral<Set<io.bootique.jdbc.DataSourceListener>>() {
        };

        Set<io.bootique.jdbc.DataSourceListener> set = runtime.getInstance(Key.get(typeLiteral));
        assertEquals(set.size(), 3);
    }

    @Test
    public void listeners_Injected() {
        BQRuntime runtime = TEST_FACTORY.app("-c", "classpath:io/bootique/jdbc/test/dummy-ds.yml")
                .autoLoadModules()
                .module(new BQModule() {

                    @Override
                    public void configure(Binder binder) {
                        JdbcModule.extend(binder)
                                .addDataSourceListener(TestDataSourceListener3.class)
                                .addDataSourceListener(TestDataSourceListener4.class);
                    }

                    @Singleton
                    @Provides
                    TestDataSourceListener3 provideListener3(BootLogger bootLogger) {
                        return new TestDataSourceListener3();
                    }

                    @Singleton
                    @Provides
                    TestDataSourceListener4 provideListener4(BootLogger bootLogger) {
                        return new TestDataSourceListener4();
                    }

                }).createRuntime();


        TypeLiteral<Set<io.bootique.jdbc.DataSourceListener>> typeLiteral = new TypeLiteral<Set<io.bootique.jdbc.DataSourceListener>>() {
        };

        Set<io.bootique.jdbc.DataSourceListener> set = runtime.getInstance(Key.get(typeLiteral));
        assertEquals(set.size(), 3);
    }

    static class TestDataSourceListener1 implements DataSourceListener {

        public TestDataSourceListener1() {
        }


        @Override
        public void afterStartup(String name, String jdbcUrl, DataSource dataSource) {
            System.out.print(name + "after started up!\n");
        }

    }

    static class TestDataSourceListener2 implements DataSourceListener {
    }

    static class TestDataSourceListener3 implements DataSourceListener {
    }

    static class TestDataSourceListener4 implements DataSourceListener {
    }
}
