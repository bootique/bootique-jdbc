package io.bootique.jdbc.tomcat;

import io.bootique.jdbc.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TomcatCPLazyDataSourceFactory implements DataSourceFactory {

    private Map<String, TomcatCPDataSourceFactory> configs;
    private ConcurrentMap<String, org.apache.tomcat.jdbc.pool.DataSource> dataSources;

    public TomcatCPLazyDataSourceFactory(Map<String, TomcatCPDataSourceFactory> configs) {
        this.configs = Objects.requireNonNull(configs);
        this.dataSources = new ConcurrentHashMap<>();
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
        return dataSources.computeIfAbsent(dataSourceName, name -> createDataSource(name));
    }

    @Override
    public void shutdown() {
        dataSources.values().forEach(d -> d.close());
    }

    protected org.apache.tomcat.jdbc.pool.DataSource createDataSource(String name) {
        return configs.computeIfAbsent(name, n -> {
            throw new IllegalStateException("No configuration present for DataSource named '" + name + "'");
        }).createDataSource();
    }

}
