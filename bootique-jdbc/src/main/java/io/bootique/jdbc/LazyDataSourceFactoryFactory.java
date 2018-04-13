package io.bootique.jdbc;

import com.google.inject.Injector;
import io.bootique.jdbc.managed.ManagedDataSourceFactory;
import io.bootique.jdbc.managed.ManagedDataSourceSupplier;

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

    private Map<String, ManagedDataSourceFactory> configs;

    public LazyDataSourceFactoryFactory(Map<String, ManagedDataSourceFactory> configs) {
        this.configs = Objects.requireNonNull(configs);
    }

    public LazyDataSourceFactory create(Injector injector, Set<DataSourceListener> listeners) {

        if (configs.isEmpty()) {
            return new LazyDataSourceFactory(Collections.emptyMap(), listeners);
        }

        Map<String, ManagedDataSourceSupplier> suppliers = new HashMap<>();
        configs.forEach((n, mdsf) -> suppliers.put(n, mdsf.create(n, injector)));
        return new LazyDataSourceFactory(suppliers, listeners);
    }
}
