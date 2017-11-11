package io.bootique.jdbc.instrumented.tomcat;

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
                .description("Provides metrics specific to the Tomcat DataSource");
    }
}
