package io.bootique.jdbc.instrumented.tomcat;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.JdbcModule;
import io.bootique.jdbc.instrumented.tomcat.healthcheck.TomcatHealthChecks;
import io.bootique.metrics.MetricNaming;
import io.bootique.metrics.health.HealthCheckModule;

public class JdbcTomcatInstrumentedModule implements Module {

    public static final MetricNaming METRIC_NAMING = MetricNaming.forModule(JdbcTomcatInstrumentedModule.class);

    @Override
    public void configure(Binder binder) {
        JdbcModule.extend(binder).addDataSourceListener(TomcatMetricsInitializer.class);
        HealthCheckModule.extend(binder).addHealthCheckGroup(TomcatHealthChecks.class);
    }

    @Singleton
    @Provides
    TomcatMetricsInitializer provideMetricsListener(MetricRegistry metricRegistry) {
        return new TomcatMetricsInitializer(metricRegistry);
    }

    @Singleton
    @Provides
    TomcatHealthChecks provideDataSourceHealthCheckGroup(DataSourceFactory dataSourceFactory) {
        return new TomcatHealthChecks(dataSourceFactory);
    }
}
