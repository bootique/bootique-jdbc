package io.bootique.jdbc.managed;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.inject.Injector;
import io.bootique.annotation.BQConfig;
import io.bootique.config.PolymorphicConfiguration;

/**
 * Configuration factory for specific DataSource implementations.
 *
 * @since 0.25
 */
@BQConfig("JDBC DataSource configuration.")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = ManagedDataSourceFactoryFactoryProxy.class)
public interface ManagedDataSourceFactoryFactory extends PolymorphicConfiguration {

    ManagedDataSourceFactory create(Injector injector);
}
