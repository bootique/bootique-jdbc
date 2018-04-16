package io.bootique.jdbc.instrumented.hikaricp;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariDataSource;
import io.bootique.jdbc.DataSourceListener;

import javax.sql.DataSource;

/**
 * @since 0.26
 */
public class HikariCPMetricsInitializer implements DataSourceListener {

    static final String METRIC_CATEGORY = "pool";
    static final String METRIC_NAME_WAIT = "Wait";

    static final String METRIC_NAME_TOTAL_CONNECTIONS = "TotalConnections";
    static final String METRIC_NAME_IDLE_CONNECTIONS = "IdleConnections";
    static final String METRIC_NAME_ACTIVE_CONNECTIONS = "ActiveConnections";
    static final String METRIC_NAME_PENDING_CONNECTIONS = "PendingConnections";

    private MetricRegistry metricRegistry;

    public HikariCPMetricsInitializer(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public static String waitTimerMetricName(String dataSourcePoolName) {
        return MetricRegistry.name(dataSourcePoolName, METRIC_CATEGORY, METRIC_NAME_WAIT);
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

        HikariDataSource hikariDS = (HikariDataSource) dataSource;

        addWaitTimer(hikariDS);
        addTotalConnectionsGauge(hikariDS);
        addIdleConnectionsGauge(hikariDS);
        addActiveConnectionsGauge(hikariDS);
        addPendingConnectionsGauge(hikariDS);
    }


    private void addWaitTimer(HikariDataSource ds) {
        metricRegistry.timer(waitTimerMetricName(ds.getPoolName()));
    }

    private void addTotalConnectionsGauge(HikariDataSource ds) {
        metricRegistry.register(MetricRegistry.name(ds.getPoolName(), METRIC_CATEGORY, METRIC_NAME_TOTAL_CONNECTIONS),
                (Gauge<Integer>) () -> ds.getHikariPoolMXBean().getTotalConnections());
    }

    private void addIdleConnectionsGauge(HikariDataSource ds) {
        metricRegistry.register(MetricRegistry.name(ds.getPoolName(), METRIC_CATEGORY, METRIC_NAME_IDLE_CONNECTIONS),
                (Gauge<Integer>) () -> ds.getHikariPoolMXBean().getIdleConnections());
    }

    private void addActiveConnectionsGauge(HikariDataSource ds) {
        metricRegistry.register(MetricRegistry.name(ds.getPoolName(), METRIC_CATEGORY, METRIC_NAME_ACTIVE_CONNECTIONS),
                (Gauge<Integer>) () -> ds.getHikariPoolMXBean().getActiveConnections());
    }

    private void addPendingConnectionsGauge(HikariDataSource ds) {
        metricRegistry.register(MetricRegistry.name(ds.getPoolName(), METRIC_CATEGORY, METRIC_NAME_PENDING_CONNECTIONS),
                (Gauge<Integer>) () -> ds.getHikariPoolMXBean().getThreadsAwaitingConnection());

    }
}
