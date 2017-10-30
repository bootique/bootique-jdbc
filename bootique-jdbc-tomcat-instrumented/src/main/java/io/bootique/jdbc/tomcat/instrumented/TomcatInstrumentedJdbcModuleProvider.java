package io.bootique.jdbc.tomcat.instrumented;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;

public class TomcatInstrumentedJdbcModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new TomcatInstrumentedJdbcModule();
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Extends Tomcat JdbcModule with metrics support.");
    }
}
