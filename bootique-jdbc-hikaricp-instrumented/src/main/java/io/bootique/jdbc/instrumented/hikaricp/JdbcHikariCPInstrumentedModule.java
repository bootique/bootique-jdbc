package io.bootique.jdbc.instrumented.hikaricp;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.jdbc.JdbcModule;
import io.bootique.jdbc.instrumented.hikaricp.healthcheck.HikariCPHealthChecks;
import io.bootique.jdbc.managed.ManagedDataSourceStarter;
import io.bootique.metrics.health.HealthCheckModule;

import java.util.Map;

public class JdbcHikariCPInstrumentedModule implements Module {

    @Override
    public void configure(Binder binder) {
        JdbcModule.extend(binder).addFactoryType(HikariCPInstrumentedDataSourceFactory.class);
        JdbcModule.extend(binder).addDataSourceListener(HikariCPMetricsInitializer.class);
        HealthCheckModule.extend(binder).addHealthCheckGroup(HikariCPHealthChecks.class);
    }

    @Singleton
    @Provides
    HikariCPHealthChecks provideHealthCheckGroup(Map<String, ManagedDataSourceStarter> starters) {
        return new HikariCPHealthChecks(starters);
    }

    @Singleton
    @Provides
    HikariCPMetricsInitializer provideMetricsInitializer(MetricRegistry metricRegistry) {
        return  new HikariCPMetricsInitializer(metricRegistry);
    }
}
