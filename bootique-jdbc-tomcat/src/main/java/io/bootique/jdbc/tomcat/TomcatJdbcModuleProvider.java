package io.bootique.jdbc.tomcat;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;

public class TomcatJdbcModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new TomcatJdbcModule("jdbc");
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Integrates Tomcat DataSource implementation.");
    }
}
