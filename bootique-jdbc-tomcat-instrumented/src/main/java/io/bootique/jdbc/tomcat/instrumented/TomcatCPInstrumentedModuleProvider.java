package io.bootique.jdbc.tomcat.instrumented;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.jdbc.tomcat.TomcatCPModule;

import java.util.Collection;
import java.util.Collections;

public class TomcatCPInstrumentedModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new TomcatCPInstrumentedModule();
    }

    @Override
    public Collection<Class<? extends Module>> overrides() {
        return Collections.singleton(TomcatCPModule.class);
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Extends JdbcModule with metrics support.");
    }
}
