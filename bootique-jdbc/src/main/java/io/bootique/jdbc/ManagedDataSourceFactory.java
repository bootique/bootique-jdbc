package io.bootique.jdbc;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.inject.Injector;
import io.bootique.annotation.BQConfig;
import io.bootique.config.PolymorphicConfiguration;

import java.util.Collection;

/**
 * @since 0.25
 */
@BQConfig("JDBC DataSource configuration.")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class", defaultImpl = DefaultManagedDataSourceFactory.class)
public interface ManagedDataSourceFactory extends PolymorphicConfiguration {

    ManagedDataSource createDataSource(String name, Injector injector, Collection<DataSourceListener> dataSourceListeners);
}
