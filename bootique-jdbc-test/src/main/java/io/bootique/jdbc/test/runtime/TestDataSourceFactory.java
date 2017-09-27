package io.bootique.jdbc.test.runtime;

import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.tomcat.TomcatCPDataSourceFactory;
import io.bootique.jdbc.tomcat.TomcatCPLazyDataSourceFactory;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @since 0.12
 */
public class TestDataSourceFactory implements DataSourceFactory {

    private TomcatCPLazyDataSourceFactory delegate;
    private Map<String, TomcatCPDataSourceFactory> configs;
    private ConcurrentMap<String, ManagedDataSource> dataSources;
    private Collection<DataSourceListener> dataSourceListeners;

    public TestDataSourceFactory(TomcatCPLazyDataSourceFactory delegate,
                                 Collection<DataSourceListener> dataSourceListeners,
                                 Map<String, TomcatCPDataSourceFactory> configs) {
        this.delegate = delegate;
        this.configs = configs;
        this.dataSourceListeners = dataSourceListeners;
        this.dataSources = new ConcurrentHashMap<>();
    }

    public void shutdown() {
        delegate.shutdown();

        // stop the DB after the DataSources were shutdown...
        dataSources.forEach((name, dataSource) ->
                dataSourceListeners.forEach(listener -> listener.afterShutdown(name, dataSource.getUrl()))
        );
    }

    @Override
    public Collection<String> allNames() {
        return delegate.allNames();
    }

    @Override
    public DataSource forName(String dataSourceName) {
        return dataSources.computeIfAbsent(dataSourceName, name -> createDataSource(name)).getDataSource();
    }

    protected ManagedDataSource createDataSource(String name) {

        // prepare DB for startup before we trigger DS creation
        Optional<String> url = getDbUrl(name);
        dataSourceListeners.forEach(listener -> listener.beforeStartup(name, url));

        DataSource dataSource = delegate.forName(name);

        // this callback is normally used for schema loading...
        dataSourceListeners.forEach(listener -> listener.afterStartup(name, url, dataSource));

        return new ManagedDataSource(delegate.forName(name), url);
    }

    protected Optional<String> getDbUrl(String configName) {
        TomcatCPDataSourceFactory config = configs.getOrDefault(configName, new TomcatCPDataSourceFactory());
        return Optional.ofNullable(config.getUrl());
    }
}
