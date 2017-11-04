package io.bootique.jdbc;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.bootique.annotation.BQConfig;
import io.bootique.config.PolymorphicConfiguration;

/**
 * @since 0.25
 */
@BQConfig
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public interface CPDataSourceFactory extends PolymorphicConfiguration {

    ManagedDataSource createDataSource();

    boolean isPartial();

    String getUrl();
}
