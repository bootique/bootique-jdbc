package io.bootique.jdbc;

import com.google.inject.Injector;
import io.bootique.jdbc.managed.ManagedDataSourceFactory;
import io.bootique.jdbc.managed.ManagedDataSourceFactoryFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Helper LazyDataSourceFactory loader.
 *
 * @since 0.25
 */
class LazyDataSourceFactoryFactory {

    private Map<String, ManagedDataSourceFactoryFactory> configs;

    public LazyDataSourceFactoryFactory(Map<String, ManagedDataSourceFactoryFactory> configs) {
        this.configs = Objects.requireNonNull(configs);
    }

    public LazyDataSourceFactory create(Injector injector, Set<DataSourceListener> listeners) {

        if (configs.isEmpty()) {
            return new LazyDataSourceFactory(Collections.emptyMap(), listeners);
        }

        Map<String, ManagedDataSourceFactory> factories = new HashMap<>();
        configs.forEach((n, ff) -> factories.put(n, ff.create(injector)));
        return new LazyDataSourceFactory(factories, listeners);
    }
}
