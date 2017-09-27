package io.bootique.jdbc.instrumented;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import io.bootique.jdbc.tomcat.TomcatCPDataSourceFactory;
import io.bootique.jdbc.tomcat.TomcatCPLazyDataSourceFactory;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.DataSource;

import java.util.Map;

public class InstrumentedLazyDataSourceFactory extends TomcatCPLazyDataSourceFactory {

    private MetricRegistry metricRegistry;

    public InstrumentedLazyDataSourceFactory(Map<String, TomcatCPDataSourceFactory> configs,
                                             MetricRegistry metricRegistry) {
        super(configs);
        this.metricRegistry = metricRegistry;
    }

    @Override
    protected DataSource createDataSource(String name) {
        DataSource dataSource = super.createDataSource(name);
        collectPoolMetrics(name, dataSource.getPool());
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
