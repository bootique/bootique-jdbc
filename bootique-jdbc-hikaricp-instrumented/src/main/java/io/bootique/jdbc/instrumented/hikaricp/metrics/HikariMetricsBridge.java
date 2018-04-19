package io.bootique.jdbc.instrumented.hikaricp.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.zaxxer.hikari.metrics.IMetricsTracker;
import com.zaxxer.hikari.metrics.PoolStats;

import java.util.concurrent.TimeUnit;

/**
 * @since 0.26
 */
public class HikariMetricsBridge implements IMetricsTracker {

    private static final String METRIC_CATEGORY = "pool";

    private final String poolName;
    private final Timer connectionObtainTimer;
    private final Histogram connectionUsage;
    private final Histogram connectionCreation;
    private final Meter connectionTimeoutMeter;
    private final MetricRegistry registry;

    public HikariMetricsBridge(final String poolName, final PoolStats poolStats, final MetricRegistry registry) {
        this.poolName = poolName;
        this.registry = registry;
        this.connectionObtainTimer = registry.timer(waitMetric(poolName));
        this.connectionUsage = registry.histogram(usageMetric(poolName));
        this.connectionCreation = registry.histogram(connectionCreationMetric(poolName));
        this.connectionTimeoutMeter = registry.meter(connectionTimeoutRateMetric(poolName));

        registry.register(totalConnectionsMetric(poolName), (Gauge<Integer>) () -> poolStats.getTotalConnections());
        registry.register(idleConnectionsMetric(poolName), (Gauge<Integer>) () -> poolStats.getIdleConnections());
        registry.register(activeConnectionsMetric(poolName), (Gauge<Integer>) () -> poolStats.getActiveConnections());
        registry.register(pendingConnectionsMetric(poolName), (Gauge<Integer>) () -> poolStats.getPendingThreads());
    }

    public static String waitMetric(String poolName) {
        return MetricRegistry.name(poolName, METRIC_CATEGORY, "Wait");
    }

    public static String usageMetric(String poolName) {
        return MetricRegistry.name(poolName, METRIC_CATEGORY, "Usage");
    }

    public static String connectionCreationMetric(String poolName) {
        return MetricRegistry.name(poolName, METRIC_CATEGORY, "ConnectionCreation");
    }

    public static String connectionTimeoutRateMetric(String poolName) {
        return MetricRegistry.name(poolName, METRIC_CATEGORY, "ConnectionTimeoutRate");
    }

    public static String activeConnectionsMetric(String poolName) {
        return MetricRegistry.name(poolName, METRIC_CATEGORY, "ActiveConnections");
    }

    public static String totalConnectionsMetric(String poolName) {
        return MetricRegistry.name(poolName, METRIC_CATEGORY, "TotalConnections");
    }

    public static String idleConnectionsMetric(String poolName) {
        return MetricRegistry.name(poolName, METRIC_CATEGORY, "IdleConnections");
    }

    public static String pendingConnectionsMetric(String poolName) {
        return MetricRegistry.name(poolName, METRIC_CATEGORY, "PendingConnections");
    }

    @Override
    public void close() {
        registry.remove(waitMetric(poolName));
        registry.remove(usageMetric(poolName));
        registry.remove(connectionCreationMetric(poolName));
        registry.remove(connectionTimeoutRateMetric(poolName));
        registry.remove(totalConnectionsMetric(poolName));
        registry.remove(idleConnectionsMetric(poolName));
        registry.remove(activeConnectionsMetric(poolName));
        registry.remove(pendingConnectionsMetric(poolName));
    }

    @Override
    public void recordConnectionAcquiredNanos(final long elapsedAcquiredNanos) {
        connectionObtainTimer.update(elapsedAcquiredNanos, TimeUnit.NANOSECONDS);
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
