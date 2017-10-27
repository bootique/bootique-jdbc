package io.bootique.jdbc;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LazyDataSourceFactory implements DataSourceFactory {

    private Map<String, TomcatDataSourceFactory> configs;
    private ConcurrentMap<String, org.apache.tomcat.jdbc.pool.DataSource> dataSources;
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
        dataSources.values().forEach(d -> d.close());

        // stop the DB after the DataSources were shutdown...
        dataSources.forEach((name, dataSource) ->
                dataSourceListeners.forEach(listener -> listener.afterShutdown(name, dataSource.getUrl(), dataSource))
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
        return dataSources.computeIfAbsent(dataSourceName, name -> createDataSource(name));
    }

    protected org.apache.tomcat.jdbc.pool.DataSource createDataSource(String name) {

        // prepare DB for startup before we trigger DS creation
        String url = getDbUrl(name);
        dataSourceListeners.forEach(listener -> listener.beforeStartup(name, url));
        //TODO: no dataSource to be modified on before event ?
        //dataSourceListeners.forEach(listener -> listener.beforeStartup(name, url));

        org.apache.tomcat.jdbc.pool.DataSource dataSource = configs.computeIfAbsent(name, n -> {
            throw new IllegalStateException("No configuration present for DataSource named '" + name + "'");
        }).createDataSource();

        // this callback is normally used for schema loading...
        dataSourceListeners.forEach(listener -> listener.afterStartup(name, url, dataSource));

        return dataSource;
    }

    protected String getDbUrl(String configName) {
        TomcatDataSourceFactory config = configs.getOrDefault(configName, new TomcatDataSourceFactory());
        return config.getUrl();
    }
}
