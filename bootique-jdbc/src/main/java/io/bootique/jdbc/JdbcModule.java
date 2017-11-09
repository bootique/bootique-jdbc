package io.bootique.jdbc;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
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

    @Override
    public void configure(Binder binder) {

        JdbcModule.extend(binder).initAllExtensions();

        binder.requestStaticInjection(DefaultDataSourceFactory.class);
    }

    /**
     * @param binder Guice DI binder.
     * @return an instance of extender.
     * @since 0.25
     */
    public static JdbcModuleExtender extend(Binder binder) {
        return new JdbcModuleExtender(binder);
    }

    @Singleton
    @Provides
    public DataSourceFactory createDataSource(ConfigurationFactory configFactory, BootLogger bootLogger,
                                              ShutdownManager shutdownManager, Set<DataSourceListener> listeners) {
        Map<String, CPDataSourceFactory> configs = configFactory
                .config(new TypeRef<Map<String, CPDataSourceFactory>>() {
                }, configPrefix);

        // TODO: figure out how to map LazyDataSourceFactoryFactory to config directly, bypassing configs map
        return new LazyDataSourceFactoryFactory(configs).create(shutdownManager, bootLogger, listeners);
    }

}
