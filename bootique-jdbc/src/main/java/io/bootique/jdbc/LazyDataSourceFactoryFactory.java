package io.bootique.jdbc;

import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LazyDataSourceFactoryFactory {

    private Map<String, CPDataSourceFactory> configs;

    public LazyDataSourceFactoryFactory(Map<String, CPDataSourceFactory> configs) {
        this.configs = configs;
    }

    // addresses an issue when a config is created as a side effect of defining BQ_JDBC_XYZ_PASSWORD
    // shell var and such...
    public static Map<String, CPDataSourceFactory> prunePartialConfigs(Map<String, CPDataSourceFactory> configs) {

        Map<String, CPDataSourceFactory> clean = new HashMap<>();
        configs.forEach((name, config) -> {
            if (!config.isPartial()) {
                clean.put(name, config);
            }
        });

        return clean;
    }

    public LazyDataSourceFactory create(ShutdownManager shutdownManager, BootLogger bootLogger,
                                        Set<DataSourceListener> dataSourceListeners) {
        LazyDataSourceFactory factory = new LazyDataSourceFactory(prunePartialConfigs(configs), dataSourceListeners);
        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down LazyDataSourceFactory...");
            factory.shutdown();
        });

        return factory;
    }
}
