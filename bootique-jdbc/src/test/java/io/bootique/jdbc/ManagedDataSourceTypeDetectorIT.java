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
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.DIRuntimeException;
import io.bootique.jdbc.managed.ManagedDataSourceFactory;
import io.bootique.jdbc.managed.ManagedDataSourceStarter;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import io.bootique.type.TypeRef;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@BQTest
public class ManagedDataSourceTypeDetectorIT {

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory();

    private static final String configPrefix = "jdbc";

    @Test
    public void factory_MultiLevel() {
        BQRuntime runtime = testFactory.app("-c", "classpath:io/bootique/jdbc/factory-type.yml")
                .autoLoadModules()
                .module(b -> JdbcModule.extend(b)
                        .addFactoryType(ManagedDataSourceFactoryX1.class)
                        .addFactoryType(ManagedDataSourceFactoryX2.class)
                        .addFactoryType(ManagedDataSourceFactoryX3.class)
                        .addFactoryType(ManagedDataSourceFactoryX4.class))
                .createRuntime();

        ConfigurationFactory configFactory = runtime.getInstance(ConfigurationFactory.class);
        Map<String, ManagedDataSourceFactory> configs = configFactory.config(new TypeRef<>() {
        }, configPrefix);

        assertTrue(configs.get("ds1") instanceof ManagedDataSourceFactoryX4);
        assertTrue(configs.get("ds2") instanceof ManagedDataSourceFactoryX1);

        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);

        DataSource ds1 = factory.forName("ds1");
        assertNotNull(ds1);

        DataSource ds2 = factory.forName("ds2");
        assertNotNull(ds2);
    }

    @Test
    public void factory_Hierarchical() {
        BQRuntime runtime = testFactory.app("-c", "classpath:io/bootique/jdbc/factory-type.yml")
                .autoLoadModules()
                .module(b -> JdbcModule.extend(b)
                        .addFactoryType(ManagedDataSourceFactoryX1.class)
                        .addFactoryType(ManagedDataSourceFactoryX2.class)
                        .addFactoryType(ManagedDataSourceFactoryX3.class)
                        .addFactoryType(ManagedDataSourceFactoryX33.class)
                        .addFactoryType(ManagedDataSourceFactoryX4.class)
                        .addFactoryType(ManagedDataSourceFactoryX44.class))
                .createRuntime();

        ConfigurationFactory configFactory = runtime.getInstance(ConfigurationFactory.class);
        Map<String, ManagedDataSourceFactory> configs = configFactory.config(new TypeRef<>() {
        }, configPrefix);

        assertTrue(configs.get("ds1") instanceof ManagedDataSourceFactoryX4);
        assertTrue(configs.get("ds2") instanceof ManagedDataSourceFactoryX1);

        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);

        DataSource ds1 = factory.forName("ds1");
        assertNotNull(ds1);

        DataSource ds2 = factory.forName("ds2");
        assertNotNull(ds2);
    }

    @Test
    public void factories_Hierarchical() {
        BQRuntime runtime = testFactory.app("-c", "classpath:io/bootique/jdbc/factories-type.yml")
                .autoLoadModules()
                .module(b -> JdbcModule.extend(b)
                        .addFactoryType(ManagedDataSourceFactoryX1.class)
                        .addFactoryType(ManagedDataSourceFactoryX2.class)
                        .addFactoryType(ManagedDataSourceFactoryX3.class)
                        .addFactoryType(ManagedDataSourceFactoryX33.class)
                        .addFactoryType(ManagedDataSourceFactoryY1.class)
                        .addFactoryType(ManagedDataSourceFactoryY2.class))
                .createRuntime();

        ConfigurationFactory configFactory = runtime.getInstance(ConfigurationFactory.class);
        Map<String, ManagedDataSourceFactory> configs = configFactory
                .config(new TypeRef<Map<String, ManagedDataSourceFactory>>() {
                }, configPrefix);

        assertTrue(configs.get("ds1") instanceof ManagedDataSourceFactoryX33);
        assertTrue(configs.get("ds2") instanceof ManagedDataSourceFactoryY2);

        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);

        DataSource ds1 = factory.forName("ds1");
        assertNotNull(ds1);

        DataSource ds2 = factory.forName("ds2");
        assertNotNull(ds2);
    }

    @Test
    public void factory_MultilevelNoType() {
        BQRuntime runtime = testFactory.app("-c", "classpath:io/bootique/jdbc/factory-notype.yml")
                .autoLoadModules()
                .module(b -> JdbcModule.extend(b)
                        .addFactoryType(ManagedDataSourceFactoryX1.class)
                        .addFactoryType(ManagedDataSourceFactoryX2.class)
                        .addFactoryType(ManagedDataSourceFactoryX3.class))
                .createRuntime();

        ConfigurationFactory configFactory = runtime.getInstance(ConfigurationFactory.class);
        Map<String, ManagedDataSourceFactory> configs = configFactory.config(new TypeRef<>() {
        }, configPrefix);

        assertTrue(configs.get("ds1") instanceof ManagedDataSourceFactoryX3);
        assertTrue(configs.get("ds2") instanceof ManagedDataSourceFactoryX3);

        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);

        DataSource ds1 = factory.forName("ds1");
        assertNotNull(ds1);

        DataSource ds2 = factory.forName("ds2");
        assertNotNull(ds2);
    }

    @Test
    public void factory_HierarchicalNoType_Exception() {
        BQRuntime runtime = testFactory.app("-c", "classpath:io/bootique/jdbc/factory-notype.yml")
                .autoLoadModules()
                .module(b -> JdbcModule.extend(b)
                        .addFactoryType(ManagedDataSourceFactoryX1.class)
                        .addFactoryType(ManagedDataSourceFactoryX2.class)
                        .addFactoryType(ManagedDataSourceFactoryX3.class)
                        .addFactoryType(ManagedDataSourceFactoryX33.class))
                .createRuntime();

        try {
            runtime.getInstance(DataSourceFactory.class).forName("ds1");
            fail("Exception expected");
        } catch (DIRuntimeException e) {
            assertTrue(e.getCause() instanceof BootiqueException);
        }
    }

    @Test
    public void factories_NoType_Exception() {
        BQRuntime runtime = testFactory.app("-c", "classpath:io/bootique/jdbc/factory-notype.yml")
                .autoLoadModules()
                .module(b -> JdbcModule.extend(b)
                        .addFactoryType(ManagedDataSourceFactoryX1.class)
                        .addFactoryType(ManagedDataSourceFactoryX2.class)
                        .addFactoryType(ManagedDataSourceFactoryY1.class)
                        .addFactoryType(ManagedDataSourceFactoryY2.class))
                .createRuntime();

        try {
            runtime.getInstance(DataSourceFactory.class).forName("ds1");
            fail("Exception expected");
        } catch (DIRuntimeException e) {
            assertTrue(e.getCause() instanceof BootiqueException);
        }
    }

    @JsonTypeName("x1")
    public static class ManagedDataSourceFactoryX1 implements ManagedDataSourceFactory {

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

    @JsonTypeName("x2")
    public static class ManagedDataSourceFactoryX2 extends ManagedDataSourceFactoryX1 {

    }

    @JsonTypeName("x3")
    public static class ManagedDataSourceFactoryX3 extends ManagedDataSourceFactoryX2 {

    }

    @JsonTypeName("x33")
    public static class ManagedDataSourceFactoryX33 extends ManagedDataSourceFactoryX2 {

    }

    @JsonTypeName("x4")
    public static class ManagedDataSourceFactoryX4 extends ManagedDataSourceFactoryX3 {

    }

    @JsonTypeName("x44")
    public static class ManagedDataSourceFactoryX44 extends ManagedDataSourceFactoryX3 {

    }

    @JsonTypeName("y1")
    public static class ManagedDataSourceFactoryY1 implements ManagedDataSourceFactory {

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

    @JsonTypeName("y2")
    public static class ManagedDataSourceFactoryY2 extends ManagedDataSourceFactoryY1 {

    }

    @JsonTypeName("y3")
    public static class ManagedDataSourceFactoryY3 extends ManagedDataSourceFactoryY2 {

    }
}
