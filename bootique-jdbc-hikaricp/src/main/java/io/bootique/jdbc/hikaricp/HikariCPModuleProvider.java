package io.bootique.jdbc.hikaricp;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.jdbc.JdbcModuleProvider;

import java.util.Collection;
import java.util.Collections;

public class HikariCPModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new HikariCPModule();
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Integrates HikariCP DataSource implementation.");
    }

    @Override
    public Collection<BQModuleProvider> dependencies() {
        return Collections.singletonList(new JdbcModuleProvider());
    }
}
