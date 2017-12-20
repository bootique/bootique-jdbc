package io.bootique.jdbc;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jdbc.managed.ManagedDataSourceFactory;
import io.bootique.type.TypeRef;

import java.util.Map;

public class ConfigsProvider implements Provider<Map<String, ManagedDataSourceFactory>> {
    @Inject
    private Injector injector;

    @Override
    public Map<String, ManagedDataSourceFactory> get() {
        ConfigurationFactory configFactory = injector.getInstance(ConfigurationFactory.class);
        Map<String, ManagedDataSourceFactory> configs = configFactory
                .config(new TypeRef<Map<String, ManagedDataSourceFactory>>() {
                }, "jdbc");

        return configs;
    }
}