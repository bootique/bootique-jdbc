/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.jdbc.managed;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import io.bootique.BQRuntime;
import io.bootique.BootiqueException;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.JdbcModule;
import io.bootique.test.junit.BQTestFactory;
import io.bootique.type.TypeRef;
import org.junit.Rule;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class ManagedDataSourceFactoryProxyIT {

    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    private static final String configPrefix = "jdbc";

    @Test
    public void testFactory_MultiLevel() {
        BQRuntime runtime = testFactory.app("-c", "classpath:io/bootique/jdbc/factory-type.yml")
                .autoLoadModules()
                .module(b -> JdbcModule.extend(b)
                        .addFactoryType(ManagedDataSourceFactoryX1.class)
                        .addFactoryType(ManagedDataSourceFactoryX2.class)
                        .addFactoryType(ManagedDataSourceFactoryX3.class)
                        .addFactoryType(ManagedDataSourceFactoryX4.class))
                .createRuntime();

        ConfigurationFactory configFactory = runtime.getInstance(ConfigurationFactory.class);
        Map<String, ManagedDataSourceFactory> configs = configFactory
                .config(new TypeRef<Map<String, ManagedDataSourceFactory>>() {
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
    public void testFactory_Hierarchical() {
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
        Map<String, ManagedDataSourceFactory> configs = configFactory
                .config(new TypeRef<Map<String, ManagedDataSourceFactory>>() {
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
    public void testFactories_Hierarchical() {
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
    public void testFactory_MultilevelNoType() {
        BQRuntime runtime = testFactory.app("-c", "classpath:io/bootique/jdbc/factory-notype.yml")
                .autoLoadModules()
                .module(b -> JdbcModule.extend(b)
                        .addFactoryType(ManagedDataSourceFactoryX1.class)
                        .addFactoryType(ManagedDataSourceFactoryX2.class)
                        .addFactoryType(ManagedDataSourceFactoryX3.class))
                .createRuntime();

        ConfigurationFactory configFactory = runtime.getInstance(ConfigurationFactory.class);
        Map<String, ManagedDataSourceFactory> configs = configFactory
                .config(new TypeRef<Map<String, ManagedDataSourceFactory>>() {
                }, configPrefix);

        assertTrue(configs.get("ds1") instanceof ManagedDataSourceFactoryProxy);
        assertTrue(configs.get("ds2") instanceof ManagedDataSourceFactoryProxy);

        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);

        DataSource ds1 = factory.forName("ds1");
        assertNotNull(ds1);

        DataSource ds2 = factory.forName("ds2");
        assertNotNull(ds2);
    }

    @Test
    public void testFactory_HierarchicalNoType_Exception() {
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
        } catch (ProvisionException e) {
            assertTrue(e.getCause() instanceof BootiqueException);
        }
    }

    @Test
    public void testFactories_NoType_Exception() {
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
        } catch (ProvisionException e) {
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
        public ManagedDataSourceStarter create(String dataSourceName, Injector injector) {
            return new ManagedDataSourceStarter(
                    url,
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
        public ManagedDataSourceStarter create(String dataSourceName, Injector injector) {
            return new ManagedDataSourceStarter(
                    url,
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
