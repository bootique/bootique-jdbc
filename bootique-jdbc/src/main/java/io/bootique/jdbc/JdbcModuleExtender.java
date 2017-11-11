package io.bootique.jdbc;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import io.bootique.ModuleExtender;
import io.bootique.jdbc.managed.ManagedDataSourceFactoryFactory;

/**
 * @since 0.25
 */
public class JdbcModuleExtender extends ModuleExtender<JdbcModuleExtender> {

    private Multibinder<DataSourceListener> listeners;
    private Multibinder<ManagedDataSourceFactoryFactory> factories;

    public JdbcModuleExtender(Binder binder) {
        super(binder);
    }

    @Override
    public JdbcModuleExtender initAllExtensions() {
        contributeListeners();
        contributeFactories();
        return this;
    }

    public JdbcModuleExtender addFactory(ManagedDataSourceFactoryFactory factory) {
        contributeFactories().addBinding().toInstance(factory);
        return this;
    }

    public JdbcModuleExtender addFactory(Class<? extends ManagedDataSourceFactoryFactory> factoryType) {
        contributeFactories().addBinding().to(factoryType);
        return this;
    }

    public JdbcModuleExtender addDataSourceListener(DataSourceListener listener) {
        contributeListeners().addBinding().toInstance(listener);
        return this;
    }

    public JdbcModuleExtender addDataSourceListener(Class<? extends DataSourceListener> listenerType) {
        contributeListeners().addBinding().to(listenerType);
        return this;
    }

    private Multibinder<DataSourceListener> contributeListeners() {
        return listeners != null ? listeners : (listeners = newSet(DataSourceListener.class));
    }

    private Multibinder<ManagedDataSourceFactoryFactory> contributeFactories() {
        return factories != null ? factories : (factories = newSet(ManagedDataSourceFactoryFactory.class));
    }
}
