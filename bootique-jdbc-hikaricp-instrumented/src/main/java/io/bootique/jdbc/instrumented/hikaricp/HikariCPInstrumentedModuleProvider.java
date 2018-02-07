package io.bootique.jdbc.instrumented.hikaricp;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.jdbc.hikaricp.HikariCPModuleProvider;
import io.bootique.jdbc.instrumented.InstrumentedJdbcModuleProvider;
import io.bootique.metrics.MetricsModuleProvider;

import java.util.Collection;

import static java.util.Arrays.asList;

public class HikariCPInstrumentedModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new HikariCPInstrumentedModule();
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
                new HikariCPModuleProvider(),
                new InstrumentedJdbcModuleProvider(),
                new MetricsModuleProvider()
        );
    }
}
