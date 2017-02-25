package io.bootique.jdbc.instrumented;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.TomcatDataSourceFactory;
import io.bootique.jdbc.instrumented.healthcheck.DataSourceHealthCheckGroup;
import io.bootique.log.BootLogger;
import io.bootique.metrics.MetricsModule;
import io.bootique.shutdown.ShutdownManager;
import io.bootique.type.TypeRef;

import java.util.Map;

public class InstrumentedJdbcModule extends ConfigModule {

    public InstrumentedJdbcModule() {
        super("jdbc");
    }

    public InstrumentedJdbcModule(String configPrefix) {
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

        Map<String, TomcatDataSourceFactory> configs = configFactory
                .config(new TypeRef<Map<String, TomcatDataSourceFactory>>() {
                }, configPrefix);

        // TODO: figure out how to map LazyDataSourceFactoryFactory to config directly, bypassing configs map
        return new InstrumentedLazyDataSourceFactoryFactory(configs).create(shutdownManager, bootLogger, metricRegistry);
    }

    @Singleton
    @Provides
    DataSourceHealthCheckGroup provideDataSourceHealthCheckGroup(DataSourceFactory dataSourceFactory) {
        return new DataSourceHealthCheckGroup(dataSourceFactory);
    }
}
