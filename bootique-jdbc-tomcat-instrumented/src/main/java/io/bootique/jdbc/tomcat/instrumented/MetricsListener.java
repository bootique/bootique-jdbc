package io.bootique.jdbc.tomcat.instrumented;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import io.bootique.jdbc.DataSourceListener;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.DataSource;

public class MetricsListener implements DataSourceListener {

    private MetricRegistry metricRegistry;

    public MetricsListener(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void beforeStartup(String name, String jdbcUrl) {
        // do  nothing...
    }

    @Override
    public void afterStartup(String name, String jdbcUrl, javax.sql.DataSource dataSource) {
        collectPoolMetrics(name, dataSource);
    }

    @Override
    public void afterShutdown(String name, String jdbcUrl, javax.sql.DataSource dataSource) {
        // do  nothing...
    }

    void collectPoolMetrics(String name, javax.sql.DataSource dataSource) {
        DataSource tomcat = (DataSource) dataSource;
        ConnectionPool pool = tomcat.getPool();

        metricRegistry.register(MetricRegistry.name(getClass(), name, "active"), (Gauge<Integer>) () -> pool.getActive());

        metricRegistry.register(MetricRegistry.name(getClass(), name, "idle"), (Gauge<Integer>) () -> pool.getIdle());

        metricRegistry.register(MetricRegistry.name(getClass(), name, "waiting"), (Gauge<Integer>) () -> pool.getWaitCount());

        metricRegistry.register(MetricRegistry.name(getClass(), name, "size"), (Gauge<Integer>) () -> pool.getSize());
    }
}
