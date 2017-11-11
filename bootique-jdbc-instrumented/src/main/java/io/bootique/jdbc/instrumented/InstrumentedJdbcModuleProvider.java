package io.bootique.jdbc.instrumented;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;

public class InstrumentedJdbcModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new InstrumentedJdbcModule();
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Add metrics and healthchecks support to JdbcModule.");
    }
}
