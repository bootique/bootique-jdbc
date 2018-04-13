package io.bootique.jdbc.instrumented.hikaricp;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.jdbc.JdbcModule;
import io.bootique.jdbc.instrumented.hikaricp.healthcheck.HikariCPHealthCheckGroup;
import io.bootique.jdbc.managed.ManagedDataSourceStarter;
import io.bootique.metrics.health.HealthCheckModule;

import java.util.Map;

public class HikariCPInstrumentedModule implements Module {

    @Override
    public void configure(Binder binder) {

        JdbcModule.extend(binder).addFactoryType(HikariCPInstrumentedDataSourceFactory.class);

        HealthCheckModule.extend(binder).addHealthCheckGroup(HikariCPHealthCheckGroup.class);
    }

    @Singleton
    @Provides
    HikariCPHealthCheckGroup provideHealthCheckGroup(Map<String, ManagedDataSourceStarter> starters) {
        return new HikariCPHealthCheckGroup(starters);
    }
}
