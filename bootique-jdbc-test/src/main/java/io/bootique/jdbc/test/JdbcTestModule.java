package io.bootique.jdbc.test;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.instrumented.InstrumentedLazyDataSourceFactoryFactory;
import io.bootique.jdbc.test.derby.DerbyListener;
import io.bootique.jdbc.test.runtime.DataSourceListener;
import io.bootique.jdbc.test.runtime.DatabaseChannelFactory;
import io.bootique.jdbc.test.runtime.DatabaseChannelFactoryFactory;
import io.bootique.jdbc.test.runtime.TestDataSourceFactory;
import io.bootique.jdbc.tomcat.TomcatCPDataSourceFactory;
import io.bootique.jdbc.tomcat.TomcatCPLazyDataSourceFactory;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;
import io.bootique.type.TypeRef;

import java.util.Map;
import java.util.Set;

/**
 * @since 0.12
 */
public class JdbcTestModule extends ConfigModule {

    /**
     * @param binder Guice DI binder.
     * @return DataSourceListener binder.
     * @deprecated since 0.24 in favor of {@link #extend(Binder)}.
     */
    @Deprecated
    public static Multibinder<DataSourceListener> contributeDataSourceListeners(Binder binder) {
        return Multibinder.newSetBinder(binder, DataSourceListener.class);
    }

    /**
     * @param binder Guice DI binder.
     * @return an instance of extender.
     * @since 0.24
     */
    public static JdbcTestModuleExtender extend(Binder binder) {
        return new JdbcTestModuleExtender(binder);
    }

    @Override
    public void configure(Binder binder) {

        // for now we only support Derby...
        JdbcTestModule.extend(binder).initAllExtensions().addDataSourceListener(DerbyListener.class);
    }

    @Singleton
    @Provides
    DataSourceFactory provideDataSourceFactory(ConfigurationFactory configFactory,
                                               BootLogger bootLogger,
                                               MetricRegistry metricRegistry,
                                               Set<DataSourceListener> dataSourceListeners,
                                               ShutdownManager shutdownManager) {

        // TODO: replace this with DI decoration of the base DataSourceFactory instead of repeating base module code

        TypeRef<Map<String, TomcatCPDataSourceFactory>> typeRef = new TypeRef<Map<String, TomcatCPDataSourceFactory>>() {
        };

        Map<String, TomcatCPDataSourceFactory> configs = configFactory.config(
                typeRef,
                // using overridden module config prefix
                "jdbc");

        TomcatCPLazyDataSourceFactory delegate =
                new InstrumentedLazyDataSourceFactoryFactory(configs)
                        .create(shutdownManager, bootLogger, metricRegistry);

        TestDataSourceFactory factory = new TestDataSourceFactory(delegate, dataSourceListeners, configs);
        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down TestDataSourceFactory...");
            factory.shutdown();
        });

        return factory;
    }

    @Singleton
    @Provides
    DatabaseChannelFactory provideDatabaseChannelFactory(
            ConfigurationFactory configFactory,
            DataSourceFactory dataSourceFactory) {
        return configFactory.config(DatabaseChannelFactoryFactory.class, configPrefix).createFactory(dataSourceFactory);
    }

    @Singleton
    @Provides
    DerbyListener provideDerbyLifecycleListener(BootLogger bootLogger) {
        return new DerbyListener(bootLogger);
    }
}
