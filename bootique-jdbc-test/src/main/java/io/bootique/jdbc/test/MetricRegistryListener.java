package io.bootique.jdbc.test;


import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import io.bootique.jdbc.DataSourceListener;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.DataSource;

import java.util.Optional;

public class MetricRegistryListener implements DataSourceListener<DataSource> {

    private MetricRegistry metricRegistry;

    public MetricRegistryListener(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void beforeStartup(String name, Optional<String> jdbcUrl) {
        //do nothing...
    }

    @Override
    public void afterStartup(String name, Optional<String> jdbcUrl, DataSource dataSource) {
        //do nothing...
    }

    @Override
    public void afterStartup(String name, DataSource dataSource) {

        //metrics on connection pool registration
        collectPoolMetrics(name, dataSource.getPool());
    }

    @Override
    public void afterShutdown(String name, Optional<String> jdbcUrl) {
        //do nothing...
    }

    @Override
    public void afterShutdown(String name, DataSource dataSource) {

    }

    void collectPoolMetrics(String name, ConnectionPool pool) {
        metricRegistry.register(MetricRegistry.name(getClass(), name, "active"), (Gauge<Integer>) () -> pool.getActive());

        metricRegistry.register(MetricRegistry.name(getClass(), name, "idle"), (Gauge<Integer>) () -> pool.getIdle());

        metricRegistry.register(MetricRegistry.name(getClass(), name, "waiting"), (Gauge<Integer>) () -> pool.getWaitCount());

        metricRegistry.register(MetricRegistry.name(getClass(), name, "size"), (Gauge<Integer>) () -> pool.getSize());
    }
}
