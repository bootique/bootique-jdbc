package io.bootique.jdbc.instrumented.hikaricp;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;

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
}
