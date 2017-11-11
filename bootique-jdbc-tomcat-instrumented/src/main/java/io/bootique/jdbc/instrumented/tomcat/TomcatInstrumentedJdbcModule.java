package io.bootique.jdbc.instrumented.tomcat;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.jdbc.JdbcModule;

public class TomcatInstrumentedJdbcModule implements Module {

    @Override
    public void configure(Binder binder) {
        JdbcModule.extend(binder).addDataSourceListener(TomcatMetricsInitializer.class);
    }

    @Singleton
    @Provides
    TomcatMetricsInitializer provideMetricsListener(MetricRegistry metricRegistry) {
        return new TomcatMetricsInitializer(metricRegistry);
    }
}
