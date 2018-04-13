package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.HealthCheckGroup;
import io.bootique.metrics.health.check.DeferredHealthCheck;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@BQConfig("Configures health checks for a Hikari DataSource.")
public class HikariCPHealthCheckFactory {

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

    public HealthCheckGroup createHealthChecks(MetricRegistry registry, DataSourceFactory dsf, String dataSourceName) {

        Map<String, HealthCheck> checks = new HashMap<>(3);

        checks.put(ConnectivityCheck.healthCheckName(dataSourceName),
                new DeferredHealthCheck(createConnectivityCheck(dsf, dataSourceName)));

        checks.put(Connection99PercentCheck.healthCheckName(dataSourceName),
                new DeferredHealthCheck(createConnection99PercentCheck(registry, dsf, dataSourceName)));

        return () -> checks;
    }

    private Supplier<Optional<HealthCheck>> createConnectivityCheck(DataSourceFactory dataSourceFactory, String dataSourceName) {

        return () ->
                dataSourceFactory
                        .forNameIfStarted(dataSourceName)
                        .map(ds -> (HikariDataSource) ds)
                        .map(this::createConnectivityCheck);
    }

    private Supplier<Optional<HealthCheck>> createConnection99PercentCheck(MetricRegistry registry, DataSourceFactory dataSourceFactory, String dataSourceName) {

        return () ->
                dataSourceFactory
                        .forNameIfStarted(dataSourceName)
                        .map(ds -> (HikariDataSource) ds)
                        .map(hds -> createConnection99PercentCheck(hds, registry));
    }

    private HealthCheck createConnectivityCheck(HikariDataSource ds) {
        HikariPoolMXBean pool = ds.getHikariPoolMXBean();
        return new ConnectivityCheck(pool, connectivityCheckTimeout);
    }

    private HealthCheck createConnection99PercentCheck(HikariDataSource ds, MetricRegistry registry) {
        return new Connection99PctCheckFactory(expected99thPercentile).createHealthCheck(registry, ds.getPoolName());
    }

}