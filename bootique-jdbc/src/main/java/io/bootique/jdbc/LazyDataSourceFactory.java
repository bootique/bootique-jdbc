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
        dataSources.values().forEach(ds -> ds.shutdown());

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
