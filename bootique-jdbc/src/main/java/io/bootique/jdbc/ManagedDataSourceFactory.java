package io.bootique.jdbc;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.inject.Injector;
import io.bootique.annotation.BQConfig;
import io.bootique.config.PolymorphicConfiguration;

/**
 * @since 0.25
 */
@BQConfig("JDBC DataSource configuration.")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DefaultManagedDataSourceFactory.class)
public interface ManagedDataSourceFactory extends PolymorphicConfiguration {

    ManagedDataSource createDataSource(Injector injector);
}
