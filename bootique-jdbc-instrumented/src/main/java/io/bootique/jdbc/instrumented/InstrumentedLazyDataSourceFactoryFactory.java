package io.bootique.jdbc.instrumented;

import com.codahale.metrics.MetricRegistry;
import io.bootique.jdbc.LazyDataSourceFactoryFactory;
import io.bootique.jdbc.TomcatDataSourceFactory;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;

import java.util.Map;

public class InstrumentedLazyDataSourceFactoryFactory {

    private Map<String, TomcatDataSourceFactory> configs;

    public InstrumentedLazyDataSourceFactoryFactory(Map<String, TomcatDataSourceFactory> configs) {
        this.configs = configs;
    }

    public InstrumentedLazyDataSourceFactory create(
            ShutdownManager shutdownManager,
            BootLogger bootLogger,
            MetricRegistry metricRegistry) {

        InstrumentedLazyDataSourceFactory factory = new InstrumentedLazyDataSourceFactory(
                LazyDataSourceFactoryFactory.prunePartialConfigs(configs),
                metricRegistry);

        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down InstrumentedLazyDataSourceFactory...");
            factory.shutdown();
        });

        return factory;
    }
}
