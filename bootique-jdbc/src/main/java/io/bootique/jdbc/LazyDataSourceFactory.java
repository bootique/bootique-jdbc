package io.bootique.jdbc;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LazyDataSourceFactory implements DataSourceFactory {

    private Map<String, TomcatDataSourceFactory> configs;
    private ConcurrentMap<String, ManagedDataSource> dataSources;
    private Collection<DataSourceListener> dataSourceListeners = Collections.emptyList();

    public LazyDataSourceFactory(Map<String, TomcatDataSourceFactory> configs,
                                 Set<DataSourceListener> dataSourceListeners) {
        this.configs = Objects.requireNonNull(configs);
        this.dataSources = new ConcurrentHashMap<>();
        this.dataSourceListeners = dataSourceListeners;
    }

    /**
     * @param configs
     * @deprecated since 0.25
     */
    public LazyDataSourceFactory(Map<String, TomcatDataSourceFactory> configs) {
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

    protected ManagedDataSource createDataSource(String name) {

        TomcatDataSourceFactory factory = configs.computeIfAbsent(name, n -> {
            throw new IllegalStateException("No configuration present for DataSource named '" + name + "'");
        });

        String url = factory.getUrl();
        dataSourceListeners.forEach(listener -> listener.beforeStartup(name, url));
        org.apache.tomcat.jdbc.pool.DataSource dataSource = factory.createDataSource();
        dataSourceListeners.forEach(listener -> listener.afterStartup(name, url, dataSource));

        return new ManagedDataSource(dataSource, url, d -> dataSource.close());
    }
}
