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
        this.connectionObtainTimer = registry.timer(waitMetricName(poolName));
        this.connectionUsage = registry.histogram(usageMetricName(poolName));
        this.connectionCreation = registry.histogram(connectionCreationMetricName(poolName));
        this.connectionTimeoutMeter = registry.meter(connectionTimeoutRateMetricName(poolName));

        registry.register(totalConnectionsMetricName(poolName), (Gauge<Integer>) () -> poolStats.getTotalConnections());
        registry.register(idleConnectionsMetricName(poolName), (Gauge<Integer>) () -> poolStats.getIdleConnections());
        registry.register(activeConnectionsMetricName(poolName), (Gauge<Integer>) () -> poolStats.getActiveConnections());
        registry.register(pendingConnectionsMetricName(poolName), (Gauge<Integer>) () -> poolStats.getPendingThreads());
    }

    public static String waitMetricName(String poolName) {
        return MetricRegistry.name(poolName, METRIC_CATEGORY, "Wait");
    }

    public static String usageMetricName(String poolName) {
        return MetricRegistry.name(poolName, METRIC_CATEGORY, "Usage");
    }

    public static String connectionCreationMetricName(String poolName) {
        return MetricRegistry.name(poolName, METRIC_CATEGORY, "ConnectionCreation");
    }

    public static String connectionTimeoutRateMetricName(String poolName) {
        return MetricRegistry.name(poolName, METRIC_CATEGORY, "ConnectionTimeoutRate");
    }

    public static String activeConnectionsMetricName(String poolName) {
        return MetricRegistry.name(poolName, METRIC_CATEGORY, "ActiveConnections");
    }

    public static String totalConnectionsMetricName(String poolName) {
        return MetricRegistry.name(poolName, METRIC_CATEGORY, "TotalConnections");
    }

    public static String idleConnectionsMetricName(String poolName) {
        return MetricRegistry.name(poolName, METRIC_CATEGORY, "IdleConnections");
    }

    public static String pendingConnectionsMetricName(String poolName) {
        return MetricRegistry.name(poolName, METRIC_CATEGORY, "PendingConnections");
    }

    @Override
    public void close() {
        registry.remove(waitMetricName(poolName));
        registry.remove(usageMetricName(poolName));
        registry.remove(connectionCreationMetricName(poolName));
        registry.remove(connectionTimeoutRateMetricName(poolName));
        registry.remove(totalConnectionsMetricName(poolName));
        registry.remove(idleConnectionsMetricName(poolName));
        registry.remove(activeConnectionsMetricName(poolName));
        registry.remove(pendingConnectionsMetricName(poolName));
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
