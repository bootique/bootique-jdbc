package io.bootique.jdbc.hikaricp;


import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;
import io.bootique.type.TypeRef;

import java.util.Map;

public class HikariCPModule extends ConfigModule {

    public HikariCPModule(String configPrefix) {
        super(configPrefix);
    }

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
    }

    @Singleton
    @Provides
    public DataSourceFactory createDataSource(ConfigurationFactory configFactory, BootLogger bootLogger,
                                              ShutdownManager shutdownManager) {
        Map<String, HikariCPDataSourceFactory> configs = configFactory
                .config(new TypeRef<Map<String, HikariCPDataSourceFactory>>() {
                }, configPrefix);

        // TODO: figure out how to map LazyDataSourceFactoryFactory to config directly, bypassing configs map
        return new HikariCPLazyDataSourceFactoryFactory(configs).create(shutdownManager, bootLogger);
    }
}
