package io.bootique.jdbc;

import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;

import java.util.HashMap;
import java.util.Map;

public class LazyDataSourceFactoryFactory {

    private Map<String, TomcatDataSourceFactory> configs;

    public LazyDataSourceFactoryFactory(Map<String, TomcatDataSourceFactory> configs) {
        this.configs = configs;
    }

    // addresses an issue when a config is created as a side effect of defining BQ_JDBC_XYZ_PASSWORD
    // shell var and such...
    public static Map<String, TomcatDataSourceFactory> prunePartialConfigs(Map<String, TomcatDataSourceFactory> configs) {

        Map<String, TomcatDataSourceFactory> clean = new HashMap<>();
        configs.forEach((name, config) -> {
            if (!config.isPartial()) {
                clean.put(name, config);
            }
        });

        return clean;
    }

    public LazyDataSourceFactory create(ShutdownManager shutdownManager, BootLogger bootLogger) {
        LazyDataSourceFactory factory = new LazyDataSourceFactory(prunePartialConfigs(configs));
        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down LazyDataSourceFactory...");
            factory.shutdown();
        });

        return factory;
    }
}
