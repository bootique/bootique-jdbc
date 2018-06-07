package io.bootique.jdbc.instrumented.hikaricp;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.inject.Injector;
import com.google.inject.Provider;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.hikaricp.HikariCPManagedDataSourceFactory;
import io.bootique.jdbc.instrumented.hikaricp.healthcheck.HikariCPHealthChecksFactory;
import io.bootique.jdbc.instrumented.hikaricp.managed.InstrumentedManagedDataSourceStarter;
import io.bootique.jdbc.managed.ManagedDataSourceStarter;
import io.bootique.metrics.health.HealthCheckGroup;

import javax.sql.DataSource;
import java.util.function.Consumer;
import java.util.function.Supplier;

@BQConfig("Pooling Hikari JDBC DataSource configuration with metrics.")
@JsonTypeName("hikari-instrumented")
public class HikariCPInstrumentedDataSourceFactory extends HikariCPManagedDataSourceFactory {

    private HikariCPHealthChecksFactory health;

    @Override
    protected ManagedDataSourceStarter create(
            String dataSourceName,
            Injector injector,
            Supplier<DataSource> startup,
            Consumer<DataSource> shutdown) {

        MetricRegistry metricRegistry = injector.getInstance(MetricRegistry.class);
        Provider<DataSourceFactory> dataSourceFactoryProvider = injector.getProvider(DataSourceFactory.class);
        HealthCheckGroup healthChecks = healthChecks(metricRegistry, dataSourceFactoryProvider, dataSourceName);

        return new InstrumentedManagedDataSourceStarter(getJdbcUrl(), startup, shutdown, healthChecks);
    }

    @BQConfigProperty
    public void setHealth(HikariCPHealthChecksFactory health) {
        this.health = health;
    }

    private HealthCheckGroup healthChecks(
            MetricRegistry metricRegistry,
            Provider<DataSourceFactory> dataSourceFactoryProvider,
            String dataSourceName) {

        HikariCPHealthChecksFactory factory = this.health != null ? this.health : new HikariCPHealthChecksFactory();
        return factory.createHealthChecks(metricRegistry, dataSourceFactoryProvider, dataSourceName);
    }
}
