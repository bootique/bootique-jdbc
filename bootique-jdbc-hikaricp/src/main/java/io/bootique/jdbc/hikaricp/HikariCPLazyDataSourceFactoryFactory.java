package io.bootique.jdbc.hikaricp;

import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;

import java.util.HashMap;
import java.util.Map;

public class HikariCPLazyDataSourceFactoryFactory {

    private Map<String, HikariCPDataSourceFactory> configs;

    public HikariCPLazyDataSourceFactoryFactory(Map<String, HikariCPDataSourceFactory> configs) {
        this.configs = configs;
    }

    // addresses an issue when a config is created as a side effect of defining BQ_JDBC_XYZ_PASSWORD
    // shell var and such...
    public static Map<String, HikariCPDataSourceFactory> prunePartialConfigs(Map<String, HikariCPDataSourceFactory> configs) {

        Map<String, HikariCPDataSourceFactory> clean = new HashMap<>();
        configs.forEach((name, config) -> {
            if (!config.isPartial()) {
                clean.put(name, config);
            }
        });

        return clean;
    }


    public HikariCPLazyDataSourceFactory create(ShutdownManager shutdownManager, BootLogger bootLogger) {
        HikariCPLazyDataSourceFactory factory = new HikariCPLazyDataSourceFactory(prunePartialConfigs(configs));
        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down LazyDataSourceFactory...");
            factory.shutdown();
        });

        return factory;
    }
}
