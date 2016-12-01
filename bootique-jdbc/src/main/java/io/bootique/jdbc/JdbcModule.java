package io.bootique.jdbc;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.BQCoreModule;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;
import io.bootique.type.TypeRef;

import java.util.Map;
import java.util.logging.Level;

public class JdbcModule extends ConfigModule {

    public JdbcModule() {
    }

    public JdbcModule(String configPrefix) {
        super(configPrefix);
    }

    @Override
    public void configure(Binder binder) {

        // suppress Tomcat PooledConnection logger. It logs some absolutely benign stuff as WARN
        // per https://github.com/bootique/bootique-jdbc/issues/25

        // it only works partially, namely when SLF intercepts JUL (e.g. under bootique-logback and such).

        // TODO: submit a patch to Tomcat to reduce this log level down...

        BQCoreModule.contributeLogLevels(binder)
                .addBinding(org.apache.tomcat.jdbc.pool.PooledConnection.class.getName())
                .toInstance(Level.OFF);
    }

    @Singleton
    @Provides
    public DataSourceFactory createDataSource(ConfigurationFactory configFactory, BootLogger bootLogger,
                                              ShutdownManager shutdownManager) {
        Map<String, TomcatDataSourceFactory> configs = configFactory
                .config(new TypeRef<Map<String, TomcatDataSourceFactory>>() {
                }, configPrefix);

        // TODO: figure out how to map LazyDataSourceFactoryFactory to config directly, bypassing configs map
        return new LazyDataSourceFactoryFactory(configs).create(shutdownManager, bootLogger);
    }

}
