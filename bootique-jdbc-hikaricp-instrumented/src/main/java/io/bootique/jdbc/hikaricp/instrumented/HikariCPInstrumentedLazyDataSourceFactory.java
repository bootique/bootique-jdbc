package io.bootique.jdbc.hikaricp.instrumented;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.bootique.jdbc.hikaricp.HikariCPDataSourceFactory;
import io.bootique.jdbc.hikaricp.HikariCPLazyDataSourceFactory;

import java.util.Map;

public class HikariCPInstrumentedLazyDataSourceFactory extends HikariCPLazyDataSourceFactory {

    private MetricRegistry metricRegistry;

    public HikariCPInstrumentedLazyDataSourceFactory(Map<String, HikariCPDataSourceFactory> configs,
                                                     MetricRegistry metricRegistry) {
        super(configs);
        this.metricRegistry = metricRegistry;
    }

    @Override
    protected HikariDataSource createDataSource(String name) {
        HikariDataSource dataSource = super.createDataSource(name);
        collectPoolMetrics(name, dataSource.getHikariPoolMXBean());
        return dataSource;
    }

    void collectPoolMetrics(String name, HikariPoolMXBean pool) {
        metricRegistry.register(MetricRegistry.name(getClass(), name, "active"), (Gauge<Integer>) () -> pool.getActiveConnections());

        metricRegistry.register(MetricRegistry.name(getClass(), name, "idle"), (Gauge<Integer>) () -> pool.getIdleConnections());

        metricRegistry.register(MetricRegistry.name(getClass(), name, "waiting"), (Gauge<Integer>) () -> pool.getThreadsAwaitingConnection());

        metricRegistry.register(MetricRegistry.name(getClass(), name, "total"), (Gauge<Integer>) () -> pool.getTotalConnections());
    }
}
