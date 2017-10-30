package io.bootique.jdbc.instrumented;

import com.codahale.metrics.MetricRegistry;
import io.bootique.jdbc.DataSourceListener;
import io.bootique.jdbc.LazyDataSourceFactoryFactory;
import io.bootique.jdbc.TomcatDataSourceFactory;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class InstrumentedLazyDataSourceFactoryFactory {

    private Map<String, TomcatDataSourceFactory> configs;

    public InstrumentedLazyDataSourceFactoryFactory(Map<String, TomcatDataSourceFactory> configs) {
        this.configs = configs;
    }

    /**
     * @param shutdownManager
     * @param bootLogger
     * @param metricRegistry
     * @return a newly created factory
     * @deprecated since 0.25 in favor of {@link #create(ShutdownManager, BootLogger, MetricRegistry, Set)}.
     */
    public InstrumentedLazyDataSourceFactory create(
            ShutdownManager shutdownManager,
            BootLogger bootLogger,
            MetricRegistry metricRegistry) {
        return create(shutdownManager, bootLogger, metricRegistry, Collections.emptySet());
    }

    /**
     * @param shutdownManager
     * @param bootLogger
     * @param metricRegistry
     * @param dataSourceListeners
     * @return a newly created factory
     * @since 0.25
     */
    public InstrumentedLazyDataSourceFactory create(
            ShutdownManager shutdownManager,
            BootLogger bootLogger,
            MetricRegistry metricRegistry,
            Set<DataSourceListener> dataSourceListeners) {

        InstrumentedLazyDataSourceFactory factory = new InstrumentedLazyDataSourceFactory(
                LazyDataSourceFactoryFactory.prunePartialConfigs(configs),
                metricRegistry,
                dataSourceListeners);

        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down InstrumentedLazyDataSourceFactory...");
            factory.shutdown();
        });

        return factory;
    }
}
