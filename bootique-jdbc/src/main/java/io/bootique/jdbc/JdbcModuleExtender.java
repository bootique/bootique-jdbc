/**
 *  Licensed to ObjectStyle LLC under one
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

package io.bootique.jdbc;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import io.bootique.ModuleExtender;
import io.bootique.jdbc.managed.ManagedDataSourceFactory;

/**
 * @since 0.25
 */
public class JdbcModuleExtender extends ModuleExtender<JdbcModuleExtender> {

    private static final Key<Class<? extends ManagedDataSourceFactory>> FACTORY_TYPE_KEY = Key
            .get(new TypeLiteral<Class<? extends ManagedDataSourceFactory>>() {
            });

    private Multibinder<DataSourceListener> listeners;
    private Multibinder<Class<? extends ManagedDataSourceFactory>> factoryTypes;

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
        contributeFactoryTypes().addBinding().toInstance(factoryType);
        return this;
    }

    public JdbcModuleExtender addDataSourceListener(DataSourceListener listener) {
        contributeListeners().addBinding().toInstance(listener);
        return this;
    }

    public JdbcModuleExtender addDataSourceListener(Class<? extends DataSourceListener> listenerType) {
        contributeListeners().addBinding().to(listenerType);
        return this;
    }

    private Multibinder<DataSourceListener> contributeListeners() {
        return listeners != null ? listeners : (listeners = newSet(DataSourceListener.class));
    }

    private Multibinder<Class<? extends ManagedDataSourceFactory>> contributeFactoryTypes() {
        return factoryTypes != null ? factoryTypes : (factoryTypes = newSet(FACTORY_TYPE_KEY));
    }
}
