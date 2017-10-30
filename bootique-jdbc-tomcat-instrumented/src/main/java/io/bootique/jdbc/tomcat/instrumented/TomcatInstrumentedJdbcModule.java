package io.bootique.jdbc.tomcat.instrumented;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.JdbcModule;
import io.bootique.jdbc.tomcat.instrumented.healthcheck.DataSourceHealthCheckGroup;
import io.bootique.metrics.MetricsModule;

public class TomcatInstrumentedJdbcModule extends ConfigModule {

    public TomcatInstrumentedJdbcModule() {
        super("jdbc");
    }

    public TomcatInstrumentedJdbcModule(String configPrefix) {
        super(configPrefix);
    }

    String getConfigPrefix() {
        return configPrefix;
    }

    @Override
    public void configure(Binder binder) {
        MetricsModule.extend(binder)
                .addHealthCheckGroup(DataSourceHealthCheckGroup.class);

        JdbcModule.extend(binder)
                .addDataSourceListener(MetricsListener.class);
    }

    @Singleton
    @Provides
    DataSourceHealthCheckGroup provideDataSourceHealthCheckGroup(DataSourceFactory dataSourceFactory) {
        return new DataSourceHealthCheckGroup(dataSourceFactory);
    }

    @Singleton
    @Provides
    MetricsListener provideMetricsListener(MetricRegistry metricRegistry) {
        return new MetricsListener(metricRegistry);
    }
}
