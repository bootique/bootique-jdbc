package io.bootique.jdbc;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import io.bootique.ConfigModule;
import io.bootique.jdbc.managed.ManagedDataSourceFactory;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;

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

        binder.bind(new TypeLiteral<Map<String, ManagedDataSourceFactory>>() {
        }).toProvider(ConfigsProvider.class);
    }

    @Singleton
    @Provides
    public DataSourceFactory createDataSource(
            Map<String, ManagedDataSourceFactory> configs,
            BootLogger bootLogger,
            ShutdownManager shutdownManager,
            Set<DataSourceListener> listeners,
            Injector injector) {

        LazyDataSourceFactory factory = new LazyDataSourceFactoryFactory(configs).create(injector, listeners);
        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down LazyDataSourceFactory...");
            factory.shutdown();
        });

        return factory;
    }
}
