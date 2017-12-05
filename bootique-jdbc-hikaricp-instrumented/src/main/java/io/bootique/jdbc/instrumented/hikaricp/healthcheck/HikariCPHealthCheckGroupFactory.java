package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.metrics.health.HealthCheck;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import static io.bootique.jdbc.instrumented.hikaricp.HikariCPMetricsInitializer.METRIC_NAME_WAIT;

@BQConfig("Configures HikcariCP data source health checks.")
public class HikariCPHealthCheckGroupFactory {

    public static final String CHECK_CATEGORY = "pool";
    public static final String CONNECTIVITY_CHECK = "ConnectivityCheck";
    public static final String CONNECTION_99_PERCENT = "Connection99Percent";

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

    public Map<String, HealthCheck> createHealthChecksMap(DataSourceFactory dataSourceFactory,
                                                          String dataSourceName,
                                                          MetricRegistry registry,
                                                          String poolName) {

        Map<String, HealthCheck> checks = new HashMap<>();
        checks.put(MetricRegistry.name(poolName, CHECK_CATEGORY, CONNECTIVITY_CHECK), createConnectivityCheck(dataSourceFactory, dataSourceName));

        HealthCheck expected99thPercentileCheck = createExpected99thPercentileCheck(registry, poolName);
        if (expected99thPercentileCheck != null) {
            checks.put(MetricRegistry.name(poolName, CHECK_CATEGORY, CONNECTION_99_PERCENT), expected99thPercentileCheck
            );
        }

        return checks;
    }

    private HealthCheck createExpected99thPercentileCheck(MetricRegistry registry, String poolName) {
        if (registry != null && expected99thPercentile > 0) {
            SortedMap<String, Timer> timers = registry.getTimers((name, metric) ->
                    name.equals(MetricRegistry.name(poolName, CHECK_CATEGORY, METRIC_NAME_WAIT)));

            if (!timers.isEmpty()) {
                final Timer timer = timers.entrySet().iterator().next().getValue();
                return new Connection99PercentCheck(timer, expected99thPercentile);
            }
        }

        return null;
    }

    private HealthCheck createConnectivityCheck(DataSourceFactory dataSourceFactory, String dataSourceName) {
        return new ConnectivityCheck(dataSourceFactory, dataSourceName, connectivityCheckTimeout);
    }
}
