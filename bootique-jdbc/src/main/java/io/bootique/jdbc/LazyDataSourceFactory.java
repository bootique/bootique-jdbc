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

import io.bootique.jdbc.managed.ManagedDataSource;
import io.bootique.jdbc.managed.ManagedDataSourceStarter;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LazyDataSourceFactory implements DataSourceFactory {

    private Collection<DataSourceListener> listeners;
    private Map<String, ManagedDataSourceStarter> starters;
    private ConcurrentMap<String, ManagedDataSource> dataSources;

    public LazyDataSourceFactory(
            Map<String, ManagedDataSourceStarter> starters,
            Set<DataSourceListener> listeners) {

        this.dataSources = new ConcurrentHashMap<>();
        this.starters = starters;
        this.listeners = listeners;
    }

    public void shutdown() {
        dataSources.values().forEach(ManagedDataSource::shutdown);

        dataSources.forEach((name, dataSource) ->
                listeners.forEach(listener -> listener.afterShutdown(name, dataSource.getUrl(), dataSource.getDataSource()))
        );
    }

    /**
     * @since 0.6
     */
    @Override
    public Collection<String> allNames() {
        return starters.keySet();
    }

    @Override
    public DataSource forName(String dataSourceName) {
        ManagedDataSource managedDataSource = dataSources.computeIfAbsent(dataSourceName, this::createManagedDataSource);
        return managedDataSource.getDataSource();
    }

    @Override
    public boolean isStarted(String dataSourceName) {
        // should we throw on "starters" missing this key?
        return dataSources.containsKey(dataSourceName);
    }

    protected ManagedDataSource createManagedDataSource(String name) {
        ManagedDataSourceStarter starter = starters.get(name);
        if (starter == null) {
            throw new IllegalStateException("No configuration present for DataSource named '" + name + "'");
        }

        String url = starter.getUrl();

        listeners.forEach(listener -> listener.beforeStartup(name, url));
        ManagedDataSource dataSource = starter.start();
        listeners.forEach(listener -> listener.afterStartup(name, url, dataSource.getDataSource()));

        return dataSource;
    }
}
