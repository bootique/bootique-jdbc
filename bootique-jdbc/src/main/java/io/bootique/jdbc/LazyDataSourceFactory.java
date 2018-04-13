package io.bootique.jdbc;

import io.bootique.jdbc.managed.ManagedDataSource;
import io.bootique.jdbc.managed.ManagedDataSourceSupplier;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LazyDataSourceFactory implements DataSourceFactory {

    private Collection<DataSourceListener> listeners;
    private Map<String, ManagedDataSourceSupplier> dataSourceFactories;
    private ConcurrentMap<String, ManagedDataSource> dataSources;

    public LazyDataSourceFactory(
            Map<String, ManagedDataSourceSupplier> dataSourceFactories,
            Set<DataSourceListener> listeners) {

        this.dataSourceFactories = dataSourceFactories;
        this.dataSources = new ConcurrentHashMap<>();
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
        return dataSourceFactories.keySet();
    }

    @Override
    public DataSource forName(String dataSourceName) {
        ManagedDataSource managedDataSource = dataSources.computeIfAbsent(dataSourceName, this::createManagedDataSource);
        return managedDataSource.getDataSource();
    }

    protected ManagedDataSource createManagedDataSource(String name) {
        ManagedDataSourceSupplier supplier = dataSourceFactories.get(name);
        if (supplier == null) {
            throw new IllegalStateException("No configuration present for DataSource named '" + name + "'");
        }

        String url = supplier.getUrl();

        listeners.forEach(listener -> listener.beforeStartup(name, url));
        ManagedDataSource dataSource = supplier.start();
        listeners.forEach(listener -> listener.afterStartup(name, url, dataSource.getDataSource()));

        return dataSource;
    }
}
