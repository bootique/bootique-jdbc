package io.bootique.jdbc.managed;

import java.util.Map;

public class Configs {

    private Map<String, ManagedDataSourceFactory> configs;

    public Configs(Map<String, ManagedDataSourceFactory> configs) {
        this.configs = configs;
    }

    public Map<String, ManagedDataSourceFactory> getConfigs() {
        return configs;
    }
}
