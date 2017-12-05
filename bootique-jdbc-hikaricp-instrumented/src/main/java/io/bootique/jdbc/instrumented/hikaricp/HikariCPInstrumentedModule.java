package io.bootique.jdbc.instrumented.hikaricp;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.JdbcModule;
import io.bootique.jdbc.instrumented.hikaricp.healthcheck.HikariCPHealthCheckGroup;
import io.bootique.jdbc.managed.Configs;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.HealthCheckModule;

import java.util.Map;

public class HikariCPInstrumentedModule implements Module {

    @Override
    public void configure(Binder binder) {

        JdbcModule.extend(binder).addFactoryType(HikariCPInstrumentedDataSourceFactory.class);
        JdbcModule.extend(binder).addDataSourceListener(HikariCPMetricsInitializer.class);

        HealthCheckModule.extend(binder).addHealthCheckGroup(HikariCPHealthCheckGroup.class);
    }

    @Singleton
    @Provides
    HikariCPMetricsInitializer provideMetricsListener(MetricRegistry metricRegistry) {
        return new HikariCPMetricsInitializer(metricRegistry);
    }

    @Singleton
    @Provides
    HikariCPHealthCheckGroup provideHealthCheckGroup(DataSourceFactory dataSourceFactory,
                                                     Configs configs,
                                                     MetricRegistry registry) {
        HikariCPHealthCheckGroup group = new HikariCPHealthCheckGroup();

        configs.getConfigs()
                .forEach((n, c) -> {
                    Map<String, HealthCheck> checks = ((HikariCPInstrumentedDataSourceFactory) c)
                            .createHealthChecksMap(dataSourceFactory, n, registry);
                    group.getHealthChecks().putAll(checks);
                });

        return group;
    }
}
