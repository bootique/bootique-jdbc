package io.bootique.jdbc.hikaricp.instrumented;

import com.codahale.metrics.MetricRegistry;
import io.bootique.jdbc.hikaricp.HikariCPDataSourceFactory;
import io.bootique.jdbc.hikaricp.HikariCPLazyDataSourceFactoryFactory;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;

import java.util.Map;

public class HikariCPInstrumentedLazyDataSourceFactoryFactory {

    private Map<String, HikariCPDataSourceFactory> configs;

    public HikariCPInstrumentedLazyDataSourceFactoryFactory(Map<String, HikariCPDataSourceFactory> configs) {
        this.configs = configs;
    }

    public HikariCPInstrumentedLazyDataSourceFactory create(
            ShutdownManager shutdownManager,
            BootLogger bootLogger,
            MetricRegistry metricRegistry) {

        HikariCPInstrumentedLazyDataSourceFactory factory = new HikariCPInstrumentedLazyDataSourceFactory(
                HikariCPLazyDataSourceFactoryFactory.prunePartialConfigs(configs),
                metricRegistry);

        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down InstrumentedLazyDataSourceFactory...");
            factory.shutdown();
        });

        return factory;
    }
}
