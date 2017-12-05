package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import com.codahale.metrics.Timer;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.HealthCheckOutcome;

import java.util.concurrent.TimeUnit;

/**
 * The health check checks that, on average, 99% of all calls to {@code getConnection()} obtain a
 * {@link java.sql.Connection} within a specified  number of milliseconds.
 */
public class Connection99PercentCheck implements HealthCheck {
    private final Timer waitTimer;
    private final long expected99thPercentile;

    Connection99PercentCheck(final Timer waitTimer, final long expected99thPercentile) {
        this.waitTimer = waitTimer;
        this.expected99thPercentile = expected99thPercentile;
    }

    /**
     * Checks that, on average, 99% of all calls to {@code getConnection()} obtain a
     * {@link java.sql.Connection} within a specified  number of milliseconds.
     *
     * @return {@link HealthCheckOutcome#ok()} if the 99th percentile of {@code getConnection()} calls
     * complete within {@code expected99thPercentile} milliseconds,  otherwise {@link HealthCheckOutcome#critical()}
     * @throws Exception {@inheritDoc}
     */
    @Override
    public HealthCheckOutcome check() throws Exception {
        final long the99thPercentile = TimeUnit.NANOSECONDS.toMillis(Math.round(waitTimer.getSnapshot().get99thPercentile()));
        return the99thPercentile <= expected99thPercentile ? HealthCheckOutcome.ok() : HealthCheckOutcome.critical(String.format("99th percentile connection wait time of %dms exceeds the threshold %dms", the99thPercentile, expected99thPercentile));
    }
}