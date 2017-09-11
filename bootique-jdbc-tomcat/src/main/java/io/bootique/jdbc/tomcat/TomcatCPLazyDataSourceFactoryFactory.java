package io.bootique.jdbc.tomcat;

import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;

import java.util.HashMap;
import java.util.Map;

public class TomcatCPLazyDataSourceFactoryFactory {

    private Map<String, TomcatCPDataSourceFactory> configs;

    public TomcatCPLazyDataSourceFactoryFactory(Map<String, TomcatCPDataSourceFactory> configs) {
        this.configs = configs;
    }

    // addresses an issue when a config is created as a side effect of defining BQ_JDBC_XYZ_PASSWORD
    // shell var and such...
    public static Map<String, TomcatCPDataSourceFactory> prunePartialConfigs(Map<String, TomcatCPDataSourceFactory> configs) {

        Map<String, TomcatCPDataSourceFactory> clean = new HashMap<>();
        configs.forEach((name, config) -> {
            if (!config.isPartial()) {
                clean.put(name, config);
            }
        });

        return clean;
    }

    public TomcatCPLazyDataSourceFactory create(ShutdownManager shutdownManager, BootLogger bootLogger) {
        TomcatCPLazyDataSourceFactory factory = new TomcatCPLazyDataSourceFactory(prunePartialConfigs(configs));
        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down LazyDataSourceFactory...");
            factory.shutdown();
        });

        return factory;
    }
}
