package io.bootique.jdbc;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;

public class JdbcModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new JdbcModule();
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Provides configuration for and access to named JDBC DataSources.");
    }
}
