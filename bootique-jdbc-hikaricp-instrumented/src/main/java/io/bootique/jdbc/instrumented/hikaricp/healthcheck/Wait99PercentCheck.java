package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import io.bootique.metrics.health.check.ValueRange;
import io.bootique.metrics.health.check.ValueRangeCheck;

import io.bootique.value.Duration;
import java.util.function.Supplier;

/**
 * The health check checks that, on average, 99% of all calls to {@code getConnection()} obtain a
 * {@link java.sql.Connection} within a specified  number of milliseconds.
 *
 * @since 0.25
 */
public class Wait99PercentCheck extends ValueRangeCheck<Duration> {

    public Wait99PercentCheck(ValueRange<Duration> range, Supplier<Duration> valueSupplier) {
        super(range, valueSupplier);
    }

    /**
     * Generates stable qualified name for the "connection99Percent" check.
     *
     * @param dataSourceName Bootique configuration name of the data source being checked.
     * @return qualified name bq.jdbc.[dataSourceName].connection99Percent
     */
    public static String healthCheckName(String dataSourceName) {
        return "bq.jdbc." + dataSourceName + ".connection99Percent";
    }
}
