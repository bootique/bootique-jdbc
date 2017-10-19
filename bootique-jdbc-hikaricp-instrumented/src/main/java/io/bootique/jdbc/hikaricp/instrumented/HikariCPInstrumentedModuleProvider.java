package io.bootique.jdbc.hikaricp.instrumented;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.jdbc.hikaricp.HikariCPModule;

import java.util.Collection;
import java.util.Collections;

public class HikariCPInstrumentedModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new HikariCPInstrumentedModule();
    }

    @Override
    public Collection<Class<? extends Module>> overrides() {
        return Collections.singleton(HikariCPModule.class);
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Extends JdbcModule with metrics support.");
    }
}
