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

import io.bootique.BQModuleProvider;
import io.bootique.ModuleCrate;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Injector;
import io.bootique.di.Provides;
import io.bootique.jdbc.managed.ManagedDataSourceFactory;
import io.bootique.jdbc.managed.ManagedDataSourceStarter;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;
import io.bootique.type.TypeRef;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JdbcModule implements BQModule, BQModuleProvider {

    /**
     * @param binder Bootique DI binder.
     * @return an instance of extender.
     */
    public static JdbcModuleExtender extend(Binder binder) {
        return new JdbcModuleExtender(binder);
    }

    @Override
    public ModuleCrate moduleCrate() {
        TypeRef<Map<String, ManagedDataSourceFactory>> type = new TypeRef<>() {
        };

        return ModuleCrate.of(this)
                .provider(this)
                .description("Configures and exposes named JDBC DataSources")
                .config("jdbc", type.getType())
                .build();
    }

    @Override
    public void configure(Binder binder) {
        JdbcModule.extend(binder).initAllExtensions();
    }

    @Singleton
    @Provides
    DataSourceFactory createDataSource(
            Map<String, ManagedDataSourceStarter> starters,
            BootLogger bootLogger,
            ShutdownManager shutdownManager,
            Set<DataSourceListener> listeners) {

        LazyDataSourceFactory factory = new LazyDataSourceFactory(starters, listeners);
        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down LazyDataSourceFactory...");
            factory.shutdown();
        });

        return factory;
    }

    @Singleton
    @Provides
    Map<String, ManagedDataSourceStarter> provideDataSourceStarters(
            ConfigurationFactory configurationFactory,
            Injector injector) {

        Map<String, ManagedDataSourceFactory> configs = configurationFactory
                .config(new TypeRef<Map<String, ManagedDataSourceFactory>>() {
                }, "jdbc");

        Map<String, ManagedDataSourceStarter> starters = new HashMap<>();
        configs.forEach((n, mdsf) -> starters.put(n, mdsf.create(n, injector)));

        return starters;
    }
}
