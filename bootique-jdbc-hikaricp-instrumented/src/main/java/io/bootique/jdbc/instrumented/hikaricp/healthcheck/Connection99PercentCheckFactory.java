package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.bootique.jdbc.instrumented.hikaricp.metrics.HikariMetricsBridge;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.check.DurationRangeFactory;
import io.bootique.metrics.health.check.ValueRange;
import io.bootique.value.Duration;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @since 0.26
 */
class Connection99PercentCheckFactory {

    private DurationRangeFactory thresholdsFactory;
    private TimeUnit timerUnit;

    public Connection99PercentCheckFactory(DurationRangeFactory thresholdsFactory, TimeUnit timerUnit) {
        this.thresholdsFactory = thresholdsFactory;
        this.timerUnit = timerUnit;
    }

    HealthCheck createHealthCheck(MetricRegistry registry, String dataSourceName) {
        Supplier<Duration> timerReader = getTimerReader(registry, dataSourceName);
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

    private Supplier<Duration> getTimerReader(MetricRegistry registry, String dataSourceName) {
        String metricName = HikariMetricsBridge.connectionWaitMetric(dataSourceName);
        return () -> readConnection99Percent(registry, metricName);
    }

    private Duration readConnection99Percent(MetricRegistry registry, String metricName) {
        // With Hikari the same app may report time in either ms or ns depending on platform, so we have to
        // dynamically convert our measurements to ms via the Hikari-provided timer unit...

        long msOrNano = (long) findTimer(registry, metricName).getSnapshot().get99thPercentile();

        // do we care to report nanosecond???
        return new Duration(timerUnit.toMillis(msOrNano));
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
