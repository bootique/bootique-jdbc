package io.bootique.jdbc;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import io.bootique.ModuleExtender;

/**
 * @since 0.25
 */
public class JdbcModuleExtender extends ModuleExtender<JdbcModuleExtender> {

    public JdbcModuleExtender(Binder binder) {
        super(binder);
    }

    @Override
    public JdbcModuleExtender initAllExtensions() {
        contributeDataSourceListeners();
        return this;
    }

    public JdbcModuleExtender addDataSourceListener(DataSourceListener listener) {
        contributeDataSourceListeners().addBinding().toInstance(listener);
        return this;
    }

    public JdbcModuleExtender addDataSourceListener(Class<? extends DataSourceListener> listenerType) {
        contributeDataSourceListeners().addBinding().to(listenerType);
        return this;
    }

    private Multibinder<DataSourceListener> contributeDataSourceListeners() {
        return newSet(DataSourceListener.class);
    }
}
