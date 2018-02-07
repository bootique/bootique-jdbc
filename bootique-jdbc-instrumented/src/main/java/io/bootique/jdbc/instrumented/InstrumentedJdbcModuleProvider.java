package io.bootique.jdbc.instrumented;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.jdbc.JdbcModuleProvider;
import io.bootique.metrics.health.HealthCheckModuleProvider;

import java.util.Collection;

import static java.util.Arrays.asList;

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

    @Override
    public Collection<BQModuleProvider> dependencies() {
        return asList(
                new JdbcModuleProvider(),
                new HealthCheckModuleProvider()
        );
    }
}
