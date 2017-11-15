package io.bootique.jdbc.instrumented;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.instrumented.healthcheck.DataSourceHealthCheckGroup;
import io.bootique.metrics.health.HealthCheckModule;

public class InstrumentedJdbcModule implements Module {

    @Override
    public void configure(Binder binder) {
        HealthCheckModule.extend(binder).addHealthCheckGroup(DataSourceHealthCheckGroup.class);
    }

    @Singleton
    @Provides
    DataSourceHealthCheckGroup provideDataSourceHealthCheckGroup(DataSourceFactory dataSourceFactory) {
        return new DataSourceHealthCheckGroup(dataSourceFactory);
    }
}
