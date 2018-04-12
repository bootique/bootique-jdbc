package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.metrics.health.HealthCheck;

import java.util.HashMap;
import java.util.Map;

@BQConfig("Configures HikariCP data source health checks.")
public class HikariCPHealthCheckGroupFactory {

    private long connectivityCheckTimeout;
    private long expected99thPercentile;

    @BQConfigProperty("Specifies a timeout for connectivity check.")
    public void setConnectivityCheckTimeout(long connectivityCheckTimeout) {
        this.connectivityCheckTimeout = connectivityCheckTimeout;
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

        checks.put(Connection99PctCheckFactory.healthCheckName(dataSourceName),
                new Connection99PctCheckFactory(expected99thPercentile).createHealthCheck(registry, ds.getPoolName()));

        return checks;
    }

    private HealthCheck createConnectivityCheck(HikariPoolMXBean pool) {
        return new ConnectivityCheck(pool, connectivityCheckTimeout);
    }
}