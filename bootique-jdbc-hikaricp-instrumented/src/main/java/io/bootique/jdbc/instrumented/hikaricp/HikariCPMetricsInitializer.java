package io.bootique.jdbc.instrumented.hikaricp;

import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariDataSource;
import io.bootique.jdbc.DataSourceListener;

import javax.sql.DataSource;

public class HikariCPMetricsInitializer implements DataSourceListener {

    static final String METRIC_CATEGORY = "pool";
    public static final String METRIC_NAME_WAIT = "Wait";
    static final String METRIC_NAME_TOTAL_CONNECTIONS = "TotalConnections";
    static final String METRIC_NAME_IDLE_CONNECTIONS = "IdleConnections";
    static final String METRIC_NAME_ACTIVE_CONNECTIONS = "ActiveConnections";
    static final String METRIC_NAME_PENDING_CONNECTIONS = "PendingConnections";


    private MetricRegistry metricRegistry;

    public HikariCPMetricsInitializer(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void beforeStartup(String name, String jdbcUrl) {
        // do nothing
    }

    @Override
    public void afterStartup(String name, String jdbcUrl, DataSource dataSource) {
        collectMetrics(dataSource);
    }

    @Override
    public void afterShutdown(String name, String jdbcUrl, DataSource dataSource) {
        // do nothing
    }

    void collectMetrics(DataSource dataSource) {
        HikariDataSource hikariDS = (HikariDataSource) dataSource;

        addWaitTimer(hikariDS);

        addTotalConnectionsGauge(hikariDS);
        addIdleConnectionsGauge(hikariDS);
        addActiveConnectionsGauge(hikariDS);
        addPendingConnectionsGauge(hikariDS);
    }

    /**
     * A {@link com.codahale.metrics.Timer} instance collecting how long requesting threads
     * to {@code getConnection()} are waiting for a connection (or timeout exception) from the pool.
     *
     * @param hikariDataSource
     */
    private void addWaitTimer(HikariDataSource hikariDataSource) {
        metricRegistry.timer(MetricRegistry.name(hikariDataSource.getPoolName(), METRIC_CATEGORY, METRIC_NAME_WAIT));
    }

    /**
     * A {@link CachedGauge}, refreshed on demand at 1 second resolution,
     * indicating the total number of connections in the pool.
     *
     * @param hikariDataSource
     */
    private void addTotalConnectionsGauge(HikariDataSource hikariDataSource) {
        metricRegistry.register(MetricRegistry.name(hikariDataSource.getPoolName(), METRIC_CATEGORY, METRIC_NAME_TOTAL_CONNECTIONS),
                (Gauge<Integer>) () -> hikariDataSource.getHikariPoolMXBean().getTotalConnections());
    }

    /**
     * A {@link CachedGauge}, refreshed on demand at 1 second resolution,
     * indicating the number of idle connections in the pool.
     *
     * @param hikariDataSource
     */
    private void addIdleConnectionsGauge(HikariDataSource hikariDataSource) {
        metricRegistry.register(MetricRegistry.name(hikariDataSource.getPoolName(), METRIC_CATEGORY, METRIC_NAME_IDLE_CONNECTIONS),
                (Gauge<Integer>) () -> hikariDataSource.getHikariPoolMXBean().getIdleConnections());
    }

    /**
     * A {@link CachedGauge}, refreshed on demand at 1 second resolution,
     * indicating the number of active (in-use) connections in the pool.
     *
     * @param hikariDataSource
     */
    private void addActiveConnectionsGauge(HikariDataSource hikariDataSource) {
        metricRegistry.register(MetricRegistry.name(hikariDataSource.getPoolName(), METRIC_CATEGORY, METRIC_NAME_ACTIVE_CONNECTIONS),
                (Gauge<Integer>) () -> hikariDataSource.getHikariPoolMXBean().getActiveConnections());
    }

    /**
     * A {@link CachedGauge}, refreshed on demand at 1 second resolution,
     * indicating the number of threads awaiting connections from the pool.
     *
     * @param hikariDataSource
     */
    private void addPendingConnectionsGauge(HikariDataSource hikariDataSource) {
        metricRegistry.register(MetricRegistry.name(hikariDataSource.getPoolName(), METRIC_CATEGORY, METRIC_NAME_PENDING_CONNECTIONS),
                (Gauge<Integer>) () -> hikariDataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }
}
