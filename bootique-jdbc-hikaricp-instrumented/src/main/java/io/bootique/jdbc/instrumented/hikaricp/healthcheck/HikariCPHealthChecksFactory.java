package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariDataSource;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.HealthCheckGroup;
import io.bootique.metrics.health.check.DeferredHealthCheck;
import io.bootique.metrics.health.check.DurationRangeFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@BQConfig("Configures health checks for a Hikari DataSource.")
public class HikariCPHealthChecksFactory {

    private DurationRangeFactory connectivity;
    private DurationRangeFactory connection99Percent;

    @BQConfigProperty("Configures a time threshold for DB connectivity check.")
    public void setConnectivity(DurationRangeFactory connectivity) {
        this.connectivity = connectivity;
    }

    @BQConfigProperty("Configures thresholds for a health check verifying 99th percentile for checkout times of pooled " +
            "connections.")
    public void setConnection99Percent(DurationRangeFactory connection99Percent) {
        this.connection99Percent = connection99Percent;
    }

    public HealthCheckGroup createHealthChecks(MetricRegistry registry, DataSourceFactory dsf, String dataSourceName) {

        Map<String, HealthCheck> checks = new HashMap<>(3);

        checks.put(ConnectivityCheck.healthCheckName(dataSourceName),
                new DeferredHealthCheck(createConnectivityCheck(dsf, dataSourceName)));

        checks.put(Connection99PercentCheck.healthCheckName(dataSourceName),
                new DeferredHealthCheck(createConnection99PercentCheck(registry, dsf, dataSourceName)));

        return () -> checks;
    }

    private Supplier<Optional<HealthCheck>> createConnectivityCheck(
            DataSourceFactory dataSourceFactory,
            String dataSourceName) {

        return () ->
                dataSourceFactory
                        .forNameIfStarted(dataSourceName)
                        .map(ds -> (HikariDataSource) ds)
                        .map(ds -> new ConnectivityCheckFactory(connectivity).createHealthCheck(ds));
    }

    private Supplier<Optional<HealthCheck>> createConnection99PercentCheck(
            MetricRegistry registry,
            DataSourceFactory dataSourceFactory,
            String dataSourceName) {

        return () ->
                dataSourceFactory
                        .forNameIfStarted(dataSourceName)
                        .map(ds -> (HikariDataSource) ds)
                        .map(ds -> new Connection99PercentCheckFactory(connection99Percent)
                                .createHealthCheck(registry, dataSourceName));
    }
}