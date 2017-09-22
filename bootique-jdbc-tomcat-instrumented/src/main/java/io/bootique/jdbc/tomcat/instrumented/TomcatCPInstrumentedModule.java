package io.bootique.jdbc.tomcat.instrumented;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.tomcat.TomcatCPDataSourceFactory;
import io.bootique.jdbc.tomcat.instrumented.healthcheck.DataSourceHealthCheckGroup;
import io.bootique.log.BootLogger;
import io.bootique.metrics.MetricsModule;
import io.bootique.shutdown.ShutdownManager;
import io.bootique.type.TypeRef;

import java.util.Map;

public class TomcatCPInstrumentedModule extends ConfigModule {

    public TomcatCPInstrumentedModule() {
        super("jdbc");
    }

    public TomcatCPInstrumentedModule(String configPrefix) {
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

        Map<String, TomcatCPDataSourceFactory> configs = configFactory
                .config(new TypeRef<Map<String, TomcatCPDataSourceFactory>>() {
                }, configPrefix);

        // TODO: figure out how to map LazyDataSourceFactoryFactory to config directly, bypassing configs map
        return new TomcatCPInstrumentedLazyDataSourceFactoryFactory(configs).create(shutdownManager, bootLogger, metricRegistry);
    }

    @Singleton
    @Provides
    DataSourceHealthCheckGroup provideDataSourceHealthCheckGroup(DataSourceFactory dataSourceFactory) {
        return new DataSourceHealthCheckGroup(dataSourceFactory);
    }
}
