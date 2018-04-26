package io.bootique.jdbc.instrumented.tomcat;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import io.bootique.jdbc.DataSourceListener;
import org.apache.tomcat.jdbc.pool.ConnectionPool;

import javax.sql.DataSource;

/**
 * @since 0.25
 */
public class TomcatMetricsInitializer implements DataSourceListener {

    private MetricRegistry metricRegistry;

    public TomcatMetricsInitializer(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void beforeStartup(String name, String jdbcUrl) {
        // do nothing
    }

    @Override
    public void afterStartup(String name, String jdbcUrl, DataSource dataSource) {
        collectPoolMetrics(name, dataSource);
    }

    @Override
    public void afterShutdown(String name, String jdbcUrl, DataSource dataSource) {
        // do nothing
    }

    void collectPoolMetrics(String name, DataSource dataSource) {
        org.apache.tomcat.jdbc.pool.DataSource tomcat = (org.apache.tomcat.jdbc.pool.DataSource) dataSource;
        ConnectionPool pool = tomcat.getPool();


        metricRegistry.register(JdbcTomcatInstrumentedModule.METRIC_NAMING.name("Pool", name, "ActiveConnections"),
                (Gauge<Integer>) () -> pool.getActive());
        metricRegistry.register(JdbcTomcatInstrumentedModule.METRIC_NAMING.name("Pool", name, "IdleConnections"),
                (Gauge<Integer>) () -> pool.getIdle());
        metricRegistry.register(JdbcTomcatInstrumentedModule.METRIC_NAMING.name("Pool", name, "PendingConnections"),
                (Gauge<Integer>) () -> pool.getWaitCount());
        metricRegistry.register(JdbcTomcatInstrumentedModule.METRIC_NAMING.name("Pool", name, "Size"),
                (Gauge<Integer>) () -> pool.getSize());
    }
}
