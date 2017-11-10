package io.bootique.jdbc;

import com.google.inject.Injector;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;

import java.util.Map;
import java.util.Set;

public class LazyDataSourceFactoryFactory {

    private Map<String, CPDataSourceFactory> configs;

    public LazyDataSourceFactoryFactory(Map<String, CPDataSourceFactory> configs) {
        this.configs = configs;
    }

    public LazyDataSourceFactory create(ShutdownManager shutdownManager, BootLogger bootLogger,
                                        Set<DataSourceListener> dataSourceListeners, Injector injector) {
        LazyDataSourceFactory factory = new LazyDataSourceFactory(configs, dataSourceListeners, injector);
        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down LazyDataSourceFactory...");
            factory.shutdown();
        });

        return factory;
    }
}
