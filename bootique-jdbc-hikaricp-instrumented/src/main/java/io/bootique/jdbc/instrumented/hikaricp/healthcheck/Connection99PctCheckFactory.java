package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.bootique.jdbc.instrumented.hikaricp.HikariCPInstrumentedDataSourceFactory;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.check.ValueRange;

import java.time.Duration;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * @since 0.26
 */
class Connection99PctCheckFactory {

    private long criticalThresholdMs;

    Connection99PctCheckFactory(long criticalThresholdMs) {
        this.criticalThresholdMs = criticalThresholdMs;
    }

    HealthCheck createHealthCheck(MetricRegistry registry, String poolName) {
        Supplier<Duration> timerReader = connection99PercentSupplier(registry, poolName);
        ValueRange<Duration> range = getConnection99PercentThresholds();
        return new Connection99PercentCheck(range, timerReader);
    }

    private ValueRange<Duration> getConnection99PercentThresholds() {

        // TODO: migrate to a ValueRangeFactory and add WARNING threshold
        if (criticalThresholdMs > 0) {
            return ValueRange.builder(Duration.class)
                    .min(Duration.ZERO)
                    .critical(Duration.ofMillis(criticalThresholdMs))
                    .build();
        }

        // default range
        return ValueRange.builder(Duration.class)
                .min(Duration.ZERO)
                .critical(Duration.ofSeconds(5))
                .build();
    }

    private Supplier<Duration> connection99PercentSupplier(MetricRegistry registry, String poolName) {

        String metricName = MetricRegistry.name(poolName,
                HikariCPInstrumentedDataSourceFactory.METRIC_CATEGORY,
                HikariCPInstrumentedDataSourceFactory.METRIC_NAME_WAIT);

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
