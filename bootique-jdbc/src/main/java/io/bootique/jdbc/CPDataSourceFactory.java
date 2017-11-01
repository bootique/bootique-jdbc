package io.bootique.jdbc;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.bootique.annotation.BQConfig;
import io.bootique.config.PolymorphicConfiguration;

/**
 * @since 0.25
 */
@BQConfig
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class CPDataSourceFactory implements PolymorphicConfiguration{

    public abstract ManagedDataSource createDataSource();

    public abstract boolean isPartial();

    public abstract String getUrl();
}
