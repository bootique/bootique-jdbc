package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.check.DurationRangeFactory;
import io.bootique.metrics.health.check.ValueRange;
import io.bootique.value.Duration;

class ConnectivityCheckFactory {

    private DurationRangeFactory thresholdsFactory;

    ConnectivityCheckFactory(DurationRangeFactory thresholdsFactory) {
        this.thresholdsFactory = thresholdsFactory;
    }

    HealthCheck createHealthCheck(HikariDataSource ds) {
        HikariPoolMXBean pool = ds.getHikariPoolMXBean();
        ValueRange<Duration> range = getRange();
        return new ConnectivityCheck(pool, range);
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
}
