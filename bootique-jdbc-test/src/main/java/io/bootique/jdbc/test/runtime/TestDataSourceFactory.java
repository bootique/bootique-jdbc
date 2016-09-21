package io.bootique.jdbc.test.runtime;

import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.LazyDataSourceFactory;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @since 0.12
 */
public class TestDataSourceFactory implements DataSourceFactory {

    private static final String URL_KEY = "url";

    private LazyDataSourceFactory delegate;
    private Map<String, Map<String, String>> configs;
    private ConcurrentMap<String, ManagedDataSource> dataSources;
    private Collection<DbLifecycleListener> dbLifecycleListeners;

    public TestDataSourceFactory(LazyDataSourceFactory delegate,
                                 Collection<DbLifecycleListener> dbLifecycleListeners,
                                 Map<String, Map<String, String>> configs) {
        this.delegate = delegate;
        this.configs = configs;
        this.dbLifecycleListeners = dbLifecycleListeners;
        this.dataSources = new ConcurrentHashMap<>();
    }

    public void shutdown() {
        delegate.shutdown();

        // stop the DB after the DataSources were shutdown...
        dataSources.values().forEach(dataSource ->
                dbLifecycleListeners.forEach(listener -> listener.afterShutdown(dataSource.getUrl())));
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
        String url = getDbUrl(name);
        if (url != null) {
            dbLifecycleListeners.forEach(listener -> listener.beforeStartup(url));
        }

        return new ManagedDataSource(delegate.forName(name), url);
    }

    protected String getDbUrl(String configName) {
        Map<String, String> config = configs.getOrDefault(configName, Collections.emptyMap());

        // TODO: must have compiled config property names outside of test...
        return config.get(URL_KEY);
    }
}
