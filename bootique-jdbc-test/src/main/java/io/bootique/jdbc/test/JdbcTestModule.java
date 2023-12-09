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

import io.bootique.BQModule;
import io.bootique.ModuleCrate;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.JdbcModule;
import io.bootique.jdbc.test.derby.DerbyListener;
import io.bootique.jdbc.test.runtime.DatabaseChannelFactory;
import io.bootique.jdbc.test.runtime.DatabaseChannelFactoryFactory;
import io.bootique.log.BootLogger;

import javax.inject.Singleton;

/**
 * @deprecated as we are we phasing out JUnit 4 support in favor of JUnit 5
 */
@Deprecated(since = "3.0", forRemoval = true)
public class JdbcTestModule implements BQModule {

    private final String CONFIG_PREFIX = "jdbctest";

    @Override
    public ModuleCrate crate() {
        return ModuleCrate.of(this)
                .description("Integrates JUnit 4 test extensions for JDBC")
                .config(CONFIG_PREFIX, DatabaseChannelFactoryFactory.class)
                .build();
    }

    @Override
    public void configure(Binder binder) {
        // for now we only support Derby...
        JdbcModule.extend(binder).initAllExtensions().addDataSourceListener(DerbyListener.class);
    }

    @Singleton
    @Provides
    DatabaseChannelFactory provideDatabaseChannelFactory(
            ConfigurationFactory configFactory,
            DataSourceFactory dataSourceFactory) {

        return configFactory.config(DatabaseChannelFactoryFactory.class, CONFIG_PREFIX).createFactory(dataSourceFactory);
    }

    @Singleton
    @Provides
    DerbyListener provideDerbyLifecycleListener(BootLogger bootLogger) {
        return new DerbyListener(bootLogger);
    }
}
