package io.bootique.jdbc.instrumented.hikaricp;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.jdbc.hikaricp.JdbcHikariCPModuleProvider;
import io.bootique.metrics.MetricsModuleProvider;
import io.bootique.metrics.health.HealthCheckModuleProvider;

import java.util.Collection;

import static java.util.Arrays.asList;

public class JdbcHikariCPInstrumentedModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new JdbcHikariCPInstrumentedModule();
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Provides metrics specific to the HikcariCP DataSource");
    }

    @Override
    public Collection<BQModuleProvider> dependencies() {
        return asList(
                new JdbcHikariCPModuleProvider(),
                new MetricsModuleProvider(),
                new HealthCheckModuleProvider()
        );
    }
}
