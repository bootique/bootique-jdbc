package io.bootique.jdbc.instrumented.hikaricp;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.hikaricp.HikariCPManagedDataSourceFactory;
import io.bootique.jdbc.instrumented.hikaricp.healthcheck.HikariCPHealthCheckGroupFactory;
import io.bootique.metrics.health.HealthCheck;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@BQConfig("Pooling Hikari JDBC DataSource configuration with metrics.")
@JsonTypeName("hikari-instrumented")
public class HikariCPInstrumentedDataSourceFactory extends HikariCPManagedDataSourceFactory {

    private HikariCPHealthCheckGroupFactory health;

    public Map<String, HealthCheck> createHealthChecksMap(DataSourceFactory dataSourceFactory,
                                                          String dataSourceName,
                                                          MetricRegistry metricRegistry) {
        Objects.requireNonNull(metricRegistry,
                "Factory is in invalid state. 'metricRegistry' was not initialized");

        return health != null ? health.createHealthChecksMap(dataSourceFactory, dataSourceName, metricRegistry, getPoolName())
                : Collections.emptyMap();
    }

    @BQConfigProperty
    public void setHealth(HikariCPHealthCheckGroupFactory health) {
        this.health = health;
    }
}
