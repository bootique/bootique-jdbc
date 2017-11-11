package io.bootique.jdbc;

import com.google.inject.Injector;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LazyDataSourceFactory implements DataSourceFactory {

    private Map<String, ? extends ManagedDataSourceFactory> configs;
    private ConcurrentMap<String, ManagedDataSource> dataSources;
    private Collection<DataSourceListener> dataSourceListeners;
    private Injector injector;

    public LazyDataSourceFactory(
            Map<String, ManagedDataSourceFactory> configs,
            Set<DataSourceListener> dataSourceListeners,
            Injector injector) {

        this.configs = Objects.requireNonNull(configs);
        this.dataSources = new ConcurrentHashMap<>();
        this.dataSourceListeners = dataSourceListeners;
        this.injector = injector;
    }

    /**
     * @param configs
     * @deprecated since 0.25
     */
    public LazyDataSourceFactory(Map<String, ? extends ManagedDataSourceFactory> configs) {
        this.configs = Objects.requireNonNull(configs);
        this.dataSources = new ConcurrentHashMap<>();
    }

    public void shutdown() {
        dataSources.values().forEach(d -> d.shutdown());

        // stop the DB after the DataSources were shutdown...
        dataSources.forEach((name, dataSource) ->
                dataSourceListeners.forEach(listener -> listener.afterShutdown(name, dataSource.getUrl(), dataSource.getDataSource()))
        );
    }

    /**
     * @since 0.6
     */
    @Override
    public Collection<String> allNames() {
        return configs.keySet();
    }

    @Override
    public DataSource forName(String dataSourceName) {
        ManagedDataSource managedDataSource = dataSources.computeIfAbsent(dataSourceName, this::createManagedDataSource);
        return managedDataSource.getDataSource();
    }

    protected ManagedDataSource createManagedDataSource(String name) {
        ManagedDataSourceFactory factory = configs.get(name);
        if (factory == null) {
            throw new IllegalStateException("No configuration present for DataSource named '" + name + "'");
        }

        ManagedDataSource managedDataSource = factory.createDataSource(injector);
        String url = managedDataSource.getUrl();

        dataSourceListeners.forEach(listener -> listener.beforeStartup(name, url));
        DataSource dataSource = managedDataSource.start();
        dataSourceListeners.forEach(listener -> listener.afterStartup(name, url, dataSource));

        return managedDataSource;
    }
}
