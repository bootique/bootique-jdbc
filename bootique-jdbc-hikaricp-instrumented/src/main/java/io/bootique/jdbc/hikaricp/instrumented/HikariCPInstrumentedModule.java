package io.bootique.jdbc.hikaricp.instrumented;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.hikaricp.HikariCPDataSourceFactory;
import io.bootique.jdbc.hikaricp.instrumented.healthcheck.DataSourceHealthCheckGroup;
import io.bootique.log.BootLogger;
import io.bootique.metrics.MetricsModule;
import io.bootique.shutdown.ShutdownManager;
import io.bootique.type.TypeRef;

import java.util.Map;

public class HikariCPInstrumentedModule extends ConfigModule {

    public HikariCPInstrumentedModule() {
        super("jdbc");
    }

    public HikariCPInstrumentedModule(String configPrefix) {
        super(configPrefix);
    }

    String getConfixPrefix() {
        return configPrefix;
    }

    @Override
    public void configure(Binder binder) {
        MetricsModule.extend(binder).addHealthCheckGroup(DataSourceHealthCheckGroup.class);
    }

    @Singleton
    @Provides
    public DataSourceFactory createDataSourceFactory(ConfigurationFactory configFactory,
                                                     BootLogger bootLogger,
                                                     MetricRegistry metricRegistry,
                                                     ShutdownManager shutdownManager) {

        Map<String, HikariCPDataSourceFactory> configs = configFactory
                .config(new TypeRef<Map<String, HikariCPDataSourceFactory>>() {
                }, configPrefix);

        // TODO: figure out how to map LazyDataSourceFactoryFactory to config directly, bypassing configs map
        return new HikariCPInstrumentedLazyDataSourceFactoryFactory(configs).create(shutdownManager, bootLogger, metricRegistry);
    }

    @Singleton
    @Provides
    DataSourceHealthCheckGroup provideDataSourceHealthCheckGroup(DataSourceFactory dataSourceFactory) {
        return new DataSourceHealthCheckGroup(dataSourceFactory);
    }
}
