package io.bootique.jdbc.instrumented.hikaricp;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.jdbc.JdbcModule;

public class HikariCPInstrumentedModule implements Module {
    @Override
    public void configure(Binder binder) {
        JdbcModule.extend(binder).addDataSourceListener(HikariCPMetricsInitializer.class);
    }

    @Singleton
    @Provides
    HikariCPMetricsInitializer provideMetricsListener(MetricRegistry metricRegistry) {
        return new HikariCPMetricsInitializer(metricRegistry);
    }
}
