package io.bootique.jdbc.managed;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.inject.Injector;
import io.bootique.annotation.BQConfig;
import io.bootique.config.PolymorphicConfiguration;

import java.util.Optional;

/**
 * Configuration factory for specific DataSource implementations.
 *
 * @since 0.25
 */
@BQConfig("JDBC DataSource configuration.")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = ManagedDataSourceFactoryProxy.class)
public interface ManagedDataSourceFactory extends PolymorphicConfiguration {

    // TODO: Optional is returned to skip configs that were created due to stray BQ_ variables.
    // Once we stop supporting vars based on naming conventions, we can replace Optional<T> with just T
    Optional<ManagedDataSourceSupplier> create(Injector injector);
}
