package io.bootique.jdbc;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jdbc.managed.Configs;
import io.bootique.jdbc.managed.ManagedDataSourceFactory;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;
import io.bootique.type.TypeRef;

import java.util.Map;
import java.util.Set;

public class JdbcModule extends ConfigModule {

    public JdbcModule() {
    }

    public JdbcModule(String configPrefix) {
        super(configPrefix);
    }

    /**
     * @param binder Guice DI binder.
     * @return an instance of extender.
     * @since 0.25
     */
    public static JdbcModuleExtender extend(Binder binder) {
        return new JdbcModuleExtender(binder);
    }

    @Override
    public void configure(Binder binder) {
        JdbcModule.extend(binder).initAllExtensions();
    }

    @Singleton
    @Provides
    public DataSourceFactory createDataSource(
            Configs configs,
            BootLogger bootLogger,
            ShutdownManager shutdownManager,
            Set<DataSourceListener> listeners,
            Injector injector) {

        LazyDataSourceFactory factory = new LazyDataSourceFactoryFactory(configs.getConfigs()).create(injector, listeners);
        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down LazyDataSourceFactory...");
            factory.shutdown();
        });

        return factory;
    }

    @Singleton
    @Provides
    public Configs createConfigs(ConfigurationFactory configFactory) {
        Map<String, ManagedDataSourceFactory> configs = configFactory
                .config(new TypeRef<Map<String, ManagedDataSourceFactory>>() {
                }, configPrefix);

        return new Configs(configs);
    }
}
