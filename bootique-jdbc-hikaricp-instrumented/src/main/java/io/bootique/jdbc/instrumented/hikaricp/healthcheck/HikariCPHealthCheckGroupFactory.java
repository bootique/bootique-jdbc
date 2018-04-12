package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jdbc.instrumented.hikaricp.HikariCPInstrumentedDataSourceFactory;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.check.ValueRange;
import io.bootique.metrics.health.check.ValueRangeCheck;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@BQConfig("Configures HikariCP data source health checks.")
public class HikariCPHealthCheckGroupFactory {

    private long connectivityCheckTimeout;
    private long expected99thPercentile;

    public long getConnectivityCheckTimeout() {
        return connectivityCheckTimeout;
    }

    @BQConfigProperty("Specifies a timeout for connectivity check.")
    public void setConnectivityCheckTimeout(long connectivityCheckTimeout) {
        this.connectivityCheckTimeout = connectivityCheckTimeout;
    }

    public long getExpected99thPercentile() {
        return expected99thPercentile;
    }

    @BQConfigProperty("A health check would succeed if on average, 99% of all calls to getConnection() obtain a " +
            "Connection within a number of milliseconds specified here. ")
    public void setExpected99thPercentile(long expected99thPercentile) {
        this.expected99thPercentile = expected99thPercentile;
    }

    public Map<String, HealthCheck> createHealthChecksMap(MetricRegistry registry, HikariDataSource ds, String dataSourceName) {
        HikariPoolMXBean pool = ds.getHikariPoolMXBean();

        Map<String, HealthCheck> checks = new HashMap<>(3);
        checks.put(ConnectivityCheck.healthCheckName(dataSourceName), createConnectivityCheck(pool));
        checks.put(Connection99PercentCheck.healthCheckName(dataSourceName), createConnection99PercentCheck(registry, ds.getPoolName()));

        return checks;
    }

    private HealthCheck createConnection99PercentCheck(MetricRegistry registry, String poolName) {
        Supplier<Duration> deferredTimer = connection99PercentSupplier(registry, poolName);
        ValueRange<Duration> range = getConnection99PercentThresholds();
        return new ValueRangeCheck<>(range, deferredTimer);
    }

    private HealthCheck createConnectivityCheck(HikariPoolMXBean pool) {
        return new ConnectivityCheck(pool, connectivityCheckTimeout);
    }

    private ValueRange<Duration> getConnection99PercentThresholds() {

        // TODO: migrate to a ValueRangeFactory and add WARNING threshold
        if (expected99thPercentile > 0) {
            return ValueRange.builder(Duration.class)
                    .min(Duration.ZERO)
                    .critical(Duration.ofMillis(expected99thPercentile))
                    .build();
        }

        // default range
        return ValueRange.builder(Duration.class)
                .min(Duration.ZERO)
                .critical(Duration.ofMillis(1000))
                .build();
    }

    private Supplier<Duration> connection99PercentSupplier(MetricRegistry registry, String poolName) {

        String metricName = MetricRegistry.name(poolName,
                HikariCPInstrumentedDataSourceFactory.METRIC_CATEGORY,
                HikariCPInstrumentedDataSourceFactory.METRIC_NAME_WAIT);

        // using deferred timer resolving to allow health checks against the system with misconfigured metrics,
        // or Hikari not yet up during health check creation
        return () -> readConnection99Percent(registry, metricName);
    }

    private Duration readConnection99Percent(MetricRegistry registry, String metricName) {
        long ms = (long) findTimer(registry, metricName).getSnapshot().get99thPercentile();
        return Duration.ofMillis(ms);
    }

    private Timer findTimer(MetricRegistry registry, String name) {

        Collection<Timer> timers = registry.getTimers((n, m) -> name.equals(n)).values();
        switch (timers.size()) {
            case 0:
                throw new IllegalArgumentException("Timer not found: " + name);
            case 1:
                return timers.iterator().next();
            default:
                throw new IllegalArgumentException("More than one Timer matching the name: " + name);
        }
    }
}