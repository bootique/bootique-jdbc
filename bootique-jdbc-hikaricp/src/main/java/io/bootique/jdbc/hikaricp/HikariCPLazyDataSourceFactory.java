package io.bootique.jdbc.hikaricp;


import com.zaxxer.hikari.HikariDataSource;
import io.bootique.jdbc.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HikariCPLazyDataSourceFactory implements DataSourceFactory {

    private Map<String, HikariCPDataSourceFactory> configs;
    private ConcurrentMap<String, HikariDataSource> dataSources;

    public HikariCPLazyDataSourceFactory(Map<String, HikariCPDataSourceFactory> configs) {
        this.configs = Objects.requireNonNull(configs);
        this.dataSources = new ConcurrentHashMap<>();
    }

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

    protected HikariDataSource createDataSource(String name) {
        return configs.computeIfAbsent(name, n -> {
            throw new IllegalStateException("No configuration present for DataSource named '" + name + "'");
        }).createDataSource();
    }
}
