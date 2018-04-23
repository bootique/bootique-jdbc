package io.bootique.jdbc.instrumented.hikaricp.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.zaxxer.hikari.metrics.IMetricsTracker;
import com.zaxxer.hikari.metrics.PoolStats;
import io.bootique.jdbc.instrumented.hikaricp.JdbcHikariCPInstrumentedModule;
import io.bootique.metrics.MetricNaming;

import java.util.concurrent.TimeUnit;

/**
 * @since 0.26
 */
public class HikariMetricsBridge implements IMetricsTracker {

    private static final MetricNaming METRIC_NAMING = MetricNaming.forModule(JdbcHikariCPInstrumentedModule.class);

    private final String dataSourceName;
    private final Timer connectionWaitTimer;
    private final Histogram connectionUsage;
    private final Histogram connectionCreation;
    private final Meter connectionTimeoutMeter;
    private final MetricRegistry registry;

    public HikariMetricsBridge(String dataSourceName, PoolStats poolStats, MetricRegistry registry) {
        this.dataSourceName = dataSourceName;
        this.registry = registry;

        this.connectionWaitTimer = registry.timer(connectionWaitMetric(dataSourceName));
        this.connectionUsage = registry.histogram(connectionUsageMetric(dataSourceName));
        this.connectionCreation = registry.histogram(connectionCreationMetric(dataSourceName));
        this.connectionTimeoutMeter = registry.meter(connectionTimeoutRateMetric(dataSourceName));

        registry.register(totalConnectionsMetric(dataSourceName), (Gauge<Integer>) () -> poolStats.getTotalConnections());
        registry.register(idleConnectionsMetric(dataSourceName), (Gauge<Integer>) () -> poolStats.getIdleConnections());
        registry.register(activeConnectionsMetric(dataSourceName), (Gauge<Integer>) () -> poolStats.getActiveConnections());
        registry.register(pendingConnectionsMetric(dataSourceName), (Gauge<Integer>) () -> poolStats.getPendingThreads());
    }

    public static String connectionWaitMetric(String dataSourceName) {
        return METRIC_NAMING.name("Pool", dataSourceName, "Wait");
    }

    public static String connectionUsageMetric(String dataSourceName) {
        return METRIC_NAMING.name("Pool", dataSourceName, "Usage");
    }

    public static String connectionCreationMetric(String dataSourceName) {
        return METRIC_NAMING.name("Pool", dataSourceName, "ConnectionCreation");
    }

    public static String connectionTimeoutRateMetric(String dataSourceName) {
        return METRIC_NAMING.name("Pool", dataSourceName, "ConnectionTimeoutRate");
    }

    public static String activeConnectionsMetric(String dataSourceName) {
        return METRIC_NAMING.name("Pool", dataSourceName, "ActiveConnections");
    }

    public static String totalConnectionsMetric(String dataSourceName) {
        return METRIC_NAMING.name("Pool", dataSourceName, "TotalConnections");
    }

    public static String idleConnectionsMetric(String dataSourceName) {
        return METRIC_NAMING.name("Pool", dataSourceName, "IdleConnections");
    }

    public static String pendingConnectionsMetric(String dataSourceName) {
        return METRIC_NAMING.name("Pool", dataSourceName, "PendingConnections");
    }

    @Override
    public void close() {
        registry.remove(connectionWaitMetric(dataSourceName));
        registry.remove(connectionUsageMetric(dataSourceName));
        registry.remove(connectionCreationMetric(dataSourceName));
        registry.remove(connectionTimeoutRateMetric(dataSourceName));
        registry.remove(totalConnectionsMetric(dataSourceName));
        registry.remove(idleConnectionsMetric(dataSourceName));
        registry.remove(activeConnectionsMetric(dataSourceName));
        registry.remove(pendingConnectionsMetric(dataSourceName));
    }

    @Override
    public void recordConnectionAcquiredNanos(final long elapsedAcquiredNanos) {
        connectionWaitTimer.update(elapsedAcquiredNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public void recordConnectionUsageMillis(final long elapsedBorrowedMillis) {
        connectionUsage.update(elapsedBorrowedMillis);
    }

    @Override
    public void recordConnectionTimeout() {
        connectionTimeoutMeter.mark();
    }

    @Override
    public void recordConnectionCreatedMillis(long connectionCreatedMillis) {
        connectionCreation.update(connectionCreatedMillis);
    }
}
