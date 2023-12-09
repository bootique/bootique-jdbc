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

import io.bootique.ModuleExtender;
import io.bootique.di.Binder;
import io.bootique.di.Key;
import io.bootique.di.SetBuilder;
import io.bootique.di.TypeLiteral;
import io.bootique.jdbc.managed.ManagedDataSourceFactory;

public class JdbcModuleExtender extends ModuleExtender<JdbcModuleExtender> {

    private static final Key<Class<? extends ManagedDataSourceFactory>> FACTORY_TYPE_KEY = Key.get(new TypeLiteral<>() {
    });

    private SetBuilder<DataSourceListener> listeners;
    private SetBuilder<Class<? extends ManagedDataSourceFactory>> factoryTypes;

    public JdbcModuleExtender(Binder binder) {
        super(binder);
    }

    @Override
    public JdbcModuleExtender initAllExtensions() {
        contributeListeners();
        contributeFactoryTypes();
        return this;
    }

    public JdbcModuleExtender addFactoryType(Class<? extends ManagedDataSourceFactory> factoryType) {
        contributeFactoryTypes().addInstance(factoryType);
        return this;
    }

    public JdbcModuleExtender addDataSourceListener(DataSourceListener listener) {
        contributeListeners().addInstance(listener);
        return this;
    }

    public JdbcModuleExtender addDataSourceListener(Class<? extends DataSourceListener> listenerType) {
        contributeListeners().add(listenerType);
        return this;
    }

    private SetBuilder<DataSourceListener> contributeListeners() {
        return listeners != null ? listeners : (listeners = newSet(DataSourceListener.class));
    }

    private SetBuilder<Class<? extends ManagedDataSourceFactory>> contributeFactoryTypes() {
        return factoryTypes != null ? factoryTypes : (factoryTypes = newSet(FACTORY_TYPE_KEY));
    }
}
