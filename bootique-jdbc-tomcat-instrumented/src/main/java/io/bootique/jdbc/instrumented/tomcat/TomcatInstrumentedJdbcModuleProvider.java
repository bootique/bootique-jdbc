package io.bootique.jdbc.instrumented.tomcat;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.jdbc.tomcat.TomcatJdbcModuleProvider;
import io.bootique.metrics.MetricsModuleProvider;
import io.bootique.metrics.health.HealthCheckModuleProvider;

import java.util.Collection;

import static java.util.Arrays.asList;

public class TomcatInstrumentedJdbcModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new TomcatInstrumentedJdbcModule();
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Provides metrics specific to the Tomcat DataSource");
    }

    @Override
    public Collection<BQModuleProvider> dependencies() {
        return asList(
                new TomcatJdbcModuleProvider(),
                new MetricsModuleProvider(),
                new HealthCheckModuleProvider()
        );
    }
}
