package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.bootique.jdbc.instrumented.hikaricp.metrics.HikariMetricsBridge;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.check.DurationRangeFactory;
import io.bootique.metrics.health.check.ValueRange;
import io.bootique.value.Duration;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * @since 0.26
 */
class Connection99PercentCheckFactory {

    private DurationRangeFactory thresholdsFactory;

    public Connection99PercentCheckFactory(DurationRangeFactory thresholdsFactory) {
        this.thresholdsFactory = thresholdsFactory;
    }

    HealthCheck createHealthCheck(MetricRegistry registry, String poolName) {
        Supplier<Duration> timerReader = getTimerReader(registry, poolName);
        ValueRange<Duration> range = getRange();
        return new Connection99PercentCheck(range, timerReader);
    }

    private ValueRange<Duration> getRange() {

        if (thresholdsFactory != null) {
            return thresholdsFactory.createRange();
        }

        // default range
        return ValueRange.builder(Duration.class)
                .min(Duration.ZERO)
                .critical(new Duration(5000))
                .build();
    }

    private Supplier<Duration> getTimerReader(MetricRegistry registry, String poolName) {
        String metricName = HikariMetricsBridge.connectionWaitMetric(poolName);
        return () -> readConnection99Percent(registry, metricName);
    }

    private Duration readConnection99Percent(MetricRegistry registry, String metricName) {
        long ms = (long) findTimer(registry, metricName).getSnapshot().get99thPercentile();
        return new Duration(ms);
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
