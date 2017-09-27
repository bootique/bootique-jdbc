package io.bootique.jdbc.instrumented;

import com.codahale.metrics.MetricRegistry;
import io.bootique.jdbc.tomcat.TomcatCPDataSourceFactory;
import io.bootique.jdbc.tomcat.TomcatCPLazyDataSourceFactoryFactory;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;

import java.util.Map;

public class InstrumentedLazyDataSourceFactoryFactory {

    private Map<String, TomcatCPDataSourceFactory> configs;

    public InstrumentedLazyDataSourceFactoryFactory(Map<String, TomcatCPDataSourceFactory> configs) {
        this.configs = configs;
    }

    public InstrumentedLazyDataSourceFactory create(
            ShutdownManager shutdownManager,
            BootLogger bootLogger,
            MetricRegistry metricRegistry) {

        InstrumentedLazyDataSourceFactory factory = new InstrumentedLazyDataSourceFactory(
                TomcatCPLazyDataSourceFactoryFactory.prunePartialConfigs(configs),
                metricRegistry);

        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down InstrumentedLazyDataSourceFactory...");
            factory.shutdown();
        });

        return factory;
    }
}
