package io.bootique.jdbc.instrumented;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import io.bootique.jdbc.DataSourceListener;
import io.bootique.jdbc.LazyDataSourceFactory;
import io.bootique.jdbc.ManagedDataSource;
import io.bootique.jdbc.TomcatDataSourceFactory;
import org.apache.tomcat.jdbc.pool.ConnectionPool;

import java.util.Map;
import java.util.Set;

public class InstrumentedLazyDataSourceFactory extends LazyDataSourceFactory {

    private MetricRegistry metricRegistry;

    /**
     * @param configs
     * @param metricRegistry
     * @param dataSourceListeners
     * @since 0.25
     */
    public InstrumentedLazyDataSourceFactory(
            Map<String, TomcatDataSourceFactory> configs,
            MetricRegistry metricRegistry,
            Set<DataSourceListener> dataSourceListeners) {

        super(configs, dataSourceListeners);
        this.metricRegistry = metricRegistry;
    }

    /**
     * @param configs
     * @param metricRegistry
     * @deprecated since 0.25 in favor of {@link #InstrumentedLazyDataSourceFactory(Map, MetricRegistry, Set)}.
     */
    @Deprecated
    public InstrumentedLazyDataSourceFactory(
            Map<String, TomcatDataSourceFactory> configs,
            MetricRegistry metricRegistry) {

        super(configs);
        this.metricRegistry = metricRegistry;
    }

    @Override
    protected ManagedDataSource createDataSource(String name) {
        ManagedDataSource dataSource = super.createDataSource(name);
        org.apache.tomcat.jdbc.pool.DataSource tomcat = (org.apache.tomcat.jdbc.pool.DataSource) dataSource.getDataSource();
        collectPoolMetrics(name, tomcat.getPool());
        return dataSource;
    }

    void collectPoolMetrics(String name, ConnectionPool pool) {
        metricRegistry.register(MetricRegistry.name(getClass(), name, "active"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return pool.getActive();
            }
        });

        metricRegistry.register(MetricRegistry.name(getClass(), name, "idle"), new Gauge<Integer>() {

            @Override
            public Integer getValue() {
                return pool.getIdle();
            }
        });

        metricRegistry.register(MetricRegistry.name(getClass(), name, "waiting"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return pool.getWaitCount();
            }
        });

        metricRegistry.register(MetricRegistry.name(getClass(), name, "size"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return pool.getSize();
            }
        });
    }
}
