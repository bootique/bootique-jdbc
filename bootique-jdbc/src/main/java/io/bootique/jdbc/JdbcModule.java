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

import io.bootique.BQCoreModule;
import io.bootique.BQModule;
import io.bootique.ModuleCrate;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.jdbc.managed.ManagedDataSourceStarter;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;

import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Set;

public class JdbcModule implements BQModule {

    static final String CONFIG_PREFIX = "jdbc";

    /**
     * @param binder Bootique DI binder.
     * @return an instance of extender.
     */
    public static JdbcModuleExtender extend(Binder binder) {
        return new JdbcModuleExtender(binder);
    }

    @Override
    public ModuleCrate crate() {
        return ModuleCrate.of(this)
                .description("Configures and exposes named JDBC DataSources")
                .config(CONFIG_PREFIX, JdbcFactory.class)
                .build();
    }

    @Override
    public void configure(Binder binder) {
        JdbcModule.extend(binder).initAllExtensions();
        BQCoreModule.extend(binder).addConfigLoader(ManagedDataSourceTypeDetector.class);
    }

    @Singleton
    @Provides
    DataSourceFactory createDataSource(
            Map<String, ManagedDataSourceStarter> starters,
            BootLogger bootLogger,
            ShutdownManager shutdownManager,
            Set<DataSourceListener> listeners) {

        return shutdownManager.onShutdown(
                new LazyDataSourceFactory(starters, listeners),
                LazyDataSourceFactory::shutdown);
    }

    @Singleton
    @Provides
    Map<String, ManagedDataSourceStarter> provideDataSourceStarters(ConfigurationFactory configFactory) {
        return configFactory.config(JdbcFactory.class, CONFIG_PREFIX).create();
    }
}
