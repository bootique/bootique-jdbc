package io.bootique.jdbc;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;

public class JdbcModule extends ConfigModule {

    public JdbcModule() {
    }

    public JdbcModule(String configPrefix) {
        super(configPrefix);
    }

    @Singleton
    @Provides
    public DataSourceFactory createDataSource() {

        throw new RuntimeException("Plug in a JDBC connection pool module¬: bootique-jdbc-tomcat or bootique-jdbc-hikari!");
    }

}
