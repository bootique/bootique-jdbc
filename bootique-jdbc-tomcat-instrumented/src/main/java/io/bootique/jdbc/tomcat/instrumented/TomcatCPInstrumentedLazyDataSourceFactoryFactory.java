package io.bootique.jdbc.tomcat.instrumented;

import com.codahale.metrics.MetricRegistry;
import io.bootique.jdbc.tomcat.TomcatCPDataSourceFactory;
import io.bootique.jdbc.tomcat.TomcatCPLazyDataSourceFactoryFactory;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;

import java.util.Map;

public class TomcatCPInstrumentedLazyDataSourceFactoryFactory {

    private Map<String, TomcatCPDataSourceFactory> configs;

    public TomcatCPInstrumentedLazyDataSourceFactoryFactory(Map<String, TomcatCPDataSourceFactory> configs) {
        this.configs = configs;
    }

    public TomcatCPInstrumentedLazyDataSourceFactory create(
            ShutdownManager shutdownManager,
            BootLogger bootLogger,
            MetricRegistry metricRegistry) {

        TomcatCPInstrumentedLazyDataSourceFactory factory = new TomcatCPInstrumentedLazyDataSourceFactory(
                TomcatCPLazyDataSourceFactoryFactory.prunePartialConfigs(configs),
                metricRegistry);

        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down InstrumentedLazyDataSourceFactory...");
            factory.shutdown();
        });

        return factory;
    }
}
