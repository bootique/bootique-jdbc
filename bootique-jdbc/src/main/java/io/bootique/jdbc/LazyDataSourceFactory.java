package io.bootique.jdbc;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LazyDataSourceFactory implements DataSourceFactory {

    private Map<String, ? extends CPDataSourceFactory> configs;
    private ConcurrentMap<String, ManagedDataSource> dataSources;
    private Collection<DataSourceListener> dataSourceListeners = Collections.emptyList();

    public LazyDataSourceFactory(Map<String, CPDataSourceFactory> configs,
                                 Set<DataSourceListener> dataSourceListeners) {
        this.configs = Objects.requireNonNull(configs);
        this.dataSources = new ConcurrentHashMap<>();
        this.dataSourceListeners = dataSourceListeners;
    }

    /**
     * @param configs
     * @deprecated since 0.25
     */
    public LazyDataSourceFactory(Map<String, ? extends CPDataSourceFactory> configs) {
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
    public javax.sql.DataSource forName(String dataSourceName) {
        ManagedDataSource managedDataSource = dataSources.computeIfAbsent(dataSourceName, name -> createDataSource(name));

        return managedDataSource.getDataSource();
    }

    public ManagedDataSource createDataSource(String name) {
        CPDataSourceFactory factory = configs.computeIfAbsent(name, n -> {
            throw new IllegalStateException("No configuration present for DataSource named '" + name + "'");
        });

        String url = factory.getUrl();
        dataSourceListeners.forEach(listener -> listener.beforeStartup(name, url));
        ManagedDataSource dataSource = factory.createDataSource();
        dataSourceListeners.forEach(listener -> listener.afterStartup(name, url, dataSource.getDataSource()));

        return dataSource;
    }
}
