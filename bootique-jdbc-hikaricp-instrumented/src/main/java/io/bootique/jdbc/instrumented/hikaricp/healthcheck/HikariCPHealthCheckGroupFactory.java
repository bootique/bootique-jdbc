package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Injector;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.metrics.health.HealthCheck;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import static io.bootique.jdbc.instrumented.hikaricp.HikariCPInstrumentedDataSourceFactory.METRIC_CATEGORY;
import static io.bootique.jdbc.instrumented.hikaricp.HikariCPInstrumentedDataSourceFactory.METRIC_NAME_WAIT;

@BQConfig("Configures HikcariCP data source health checks.")
public class HikariCPHealthCheckGroupFactory {

    private static final String CONNECTIVITY_CHECK = "ConnectivityCheck";
    private static final String CONNECTION_99_PERCENT = "Connection99Percent";

    private long connectivityCheckTimeout;
    private long expected99thPercentile;

    public HikariCPHealthCheckGroupFactory() {

    }

    public long getConnectivityCheckTimeout() {
        return connectivityCheckTimeout;
    }

    @BQConfigProperty
    public void setConnectivityCheckTimeout(long connectivityCheckTimeout) {
        this.connectivityCheckTimeout = connectivityCheckTimeout;
    }

    public long getExpected99thPercentile() {
        return expected99thPercentile;
    }

    @BQConfigProperty
    public void setExpected99thPercentile(long expected99thPercentile) {
        this.expected99thPercentile = expected99thPercentile;
    }

    public Map<String, HealthCheck> createHealthChecksMap(Injector injector, HikariPoolMXBean pool, String poolName) {

        MetricRegistry registry = injector.getInstance(MetricRegistry.class);


        Map<String, HealthCheck> checks = new HashMap<>();
        checks.put(MetricRegistry.name(poolName, METRIC_CATEGORY, CONNECTIVITY_CHECK),
                createConnectivityCheck(pool));

        HealthCheck expected99thPercentileCheck = createExpected99thPercentileCheck(registry, poolName);
        if (expected99thPercentileCheck != null) {
            checks.put(MetricRegistry.name(poolName, METRIC_CATEGORY, CONNECTION_99_PERCENT), expected99thPercentileCheck
            );
        }

        return checks;
    }

    private HealthCheck createExpected99thPercentileCheck(MetricRegistry registry, String poolName) {
        if (registry != null && expected99thPercentile > 0) {
            SortedMap<String, Timer> timers = registry.getTimers((name, metric) ->
                    name.equals(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_WAIT)));

            if (!timers.isEmpty()) {
                final Timer timer = timers.entrySet().iterator().next().getValue();
                return new Connection99PercentCheck(timer, expected99thPercentile);
            }
        }

        return null;
    }

    private HealthCheck createConnectivityCheck(HikariPoolMXBean pool) {
        return new ConnectivityCheck(pool, connectivityCheckTimeout);
    }
}
