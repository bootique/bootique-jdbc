package io.bootique.jdbc.tomcat;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.jdbc.JdbcModuleProvider;

import java.util.Collection;
import java.util.Collections;

/**
 * @since 0.25
 */
public class JdbcTomcatModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new JdbcTomcatModule();
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Integrates Tomcat DataSource implementation.");
    }

    @Override
    public Collection<BQModuleProvider> dependencies() {
        return Collections.singletonList(new JdbcModuleProvider());
    }
}
