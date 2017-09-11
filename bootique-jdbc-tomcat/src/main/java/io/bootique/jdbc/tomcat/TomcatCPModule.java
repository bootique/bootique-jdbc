package io.bootique.jdbc.tomcat;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.BQCoreModule;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;
import io.bootique.type.TypeRef;

import java.util.Map;
import java.util.logging.Level;

public class TomcatCPModule extends ConfigModule {

    public TomcatCPModule(String configPrefix) {
        super(configPrefix);
    }

    @Override
    public void configure(Binder binder) {
        super.configure(binder);

        // suppress Tomcat PooledConnection logger. It logs some absolutely benign stuff as WARN
        // per https://github.com/bootique/bootique-jdbc/issues/25

        // it only works partially, namely when SLF intercepts JUL (e.g. under bootique-logback and such).

        // TODO: submit a patch to Tomcat to reduce this log level down...
        BQCoreModule.extend(binder)
                .setLogLevel(org.apache.tomcat.jdbc.pool.PooledConnection.class.getName(), Level.OFF);
    }

    @Singleton
    @Provides
    public DataSourceFactory createDataSource(ConfigurationFactory configFactory, BootLogger bootLogger,
                                              ShutdownManager shutdownManager) {
        Map<String, TomcatCPDataSourceFactory> configs = configFactory
                .config(new TypeRef<Map<String, TomcatCPDataSourceFactory>>() {
                }, configPrefix);

        // TODO: figure out how to map LazyDataSourceFactoryFactory to config directly, bypassing configs map
        return new TomcatCPLazyDataSourceFactoryFactory(configs).create(shutdownManager, bootLogger);
    }
}
