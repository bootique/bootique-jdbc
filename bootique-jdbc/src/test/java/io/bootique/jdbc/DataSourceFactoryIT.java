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

package io.bootique.jdbc;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.BQRuntime;
import io.bootique.BootiqueException;
import io.bootique.di.DIRuntimeException;
import io.bootique.jdbc.managed.ManagedDataSourceFactory;
import io.bootique.jdbc.managed.ManagedDataSourceStarter;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@BQTest
public class DataSourceFactoryIT {

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void forName_NoImpl() {

        BQRuntime runtime = testFactory.app("-c", "classpath:io/bootique/jdbc/DataSourceFactoryIT_notype.yml")
                .autoLoadModules()
                .createRuntime();

        try {
            runtime.getInstance(DataSourceFactory.class).forName("ds1");
        } catch (DIRuntimeException e) {
            assertTrue(e.getCause() instanceof BootiqueException, () -> "Unexpected exception: " + e.getCause());
            return;
        }

        fail("Exception expected");
    }

    @Test
    public void forName_SingleImpl() {

        BQRuntime runtime = testFactory.app("-c", "classpath:io/bootique/jdbc/DataSourceFactoryIT_notype.yml")
                .autoLoadModules()
                .module(b -> JdbcModule.extend(b).addFactoryType(Factory1.class))
                .createRuntime();

        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("ds1");
        assertNotNull(ds);
    }

    @Test
    public void forName_MultiImpl() {

        BQRuntime runtime = testFactory.app("-c", "classpath:io/bootique/jdbc/DataSourceFactoryIT_notype.yml")
                .autoLoadModules()
                .module(b -> JdbcModule.extend(b).addFactoryType(Factory1.class).addFactoryType(Factory2.class))
                .createRuntime();

        try {
            runtime.getInstance(DataSourceFactory.class).forName("ds1");
        } catch (DIRuntimeException e) {
            assertTrue(e.getCause() instanceof BootiqueException, () -> "Unexpected exception: " + e.getCause());
            return;
        }

        fail("Exception expected");
    }

    @Test
    public void forName_ListenerLifecycle() {

        TestListener listener = new TestListener();

        BQRuntime runtime = testFactory.app("-c", "classpath:io/bootique/jdbc/DataSourceFactoryIT_2ds.yml")
                .autoLoadModules()
                .module(b -> JdbcModule
                        .extend(b)
                        .addFactoryType(Factory1.class)
                        .addDataSourceListener(listener))
                .createRuntime();


        listener.assertEmpty();
        DataSource ds1 = runtime.getInstance(DataSourceFactory.class).forName("ds1");
        listener.assertDSStartup("jdbc:dummy1", ds1, 1);

        DataSource ds2 = runtime.getInstance(DataSourceFactory.class).forName("ds2");
        listener.assertDSStartup("jdbc:dummy2", ds2, 2);

        runtime.getInstance(DataSourceFactory.class).forName("ds2");
        listener.assertDSStartup("jdbc:dummy2", ds2, 2);

        runtime.shutdown();
        listener.assertShutdown();
    }

    @JsonTypeName("f1")
    public static class Factory1 implements ManagedDataSourceFactory {

        private String url;

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public ManagedDataSourceStarter create(String dataSourceName) {
            return new ManagedDataSourceStarter(
                    () -> url,
                    () -> mock(DataSource.class),
                    ds -> {
                    });
        }
    }

    @JsonTypeName("f2")
    public static class Factory2 implements ManagedDataSourceFactory {

        private String url;

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public ManagedDataSourceStarter create(String dataSourceName) {
            return new ManagedDataSourceStarter(
                    () -> url,
                    () -> mock(DataSource.class),
                    ds -> {
                    });
        }
    }

    private static class TestListener implements DataSourceListener {

        List<String> beforeStartup = new ArrayList<>();
        Map<String, DataSource> afterStartup = new HashMap<>();
        Map<String, DataSource> afterShutdown = new HashMap<>();

        void assertEmpty() {
            assertTrue(beforeStartup.isEmpty());
            assertTrue(afterStartup.isEmpty());
            assertTrue(afterShutdown.isEmpty());
        }

        void assertDSStartup(String url, DataSource dataSource, int totalDataSources) {
            assertEquals(totalDataSources, beforeStartup.size());
            assertEquals(totalDataSources, afterStartup.size());
            assertTrue(beforeStartup.contains(url));
            assertSame(dataSource, afterStartup.get(url));
            assertTrue(afterShutdown.isEmpty());
        }

        void assertShutdown() {
            assertEquals(afterStartup.size(), afterShutdown.size());
            assertEquals(afterStartup, afterShutdown);
        }

        @Override
        public void beforeStartup(String name, String jdbcUrl) {
            beforeStartup.add(jdbcUrl);
        }

        @Override
        public void afterStartup(String name, String jdbcUrl, DataSource dataSource) {
            afterStartup.put(jdbcUrl, dataSource);
        }

        @Override
        public void afterShutdown(String name, String jdbcUrl, DataSource dataSource) {
            afterShutdown.put(jdbcUrl, dataSource);
        }
    }
}
