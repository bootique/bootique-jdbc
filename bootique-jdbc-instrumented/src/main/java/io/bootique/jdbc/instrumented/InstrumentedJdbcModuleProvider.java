package io.bootique.jdbc.instrumented;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.jdbc.JdbcModule;

import java.util.Collection;
import java.util.Collections;

public class InstrumentedJdbcModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new InstrumentedJdbcModule();
    }

    @Override
    public Collection<Class<? extends Module>> overrides() {
        return Collections.singleton(JdbcModule.class);
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Extends JdbcModule with metrics support.");
    }
}
