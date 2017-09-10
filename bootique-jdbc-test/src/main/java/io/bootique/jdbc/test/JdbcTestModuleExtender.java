package io.bootique.jdbc.test;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import io.bootique.ModuleExtender;
import io.bootique.jdbc.test.runtime.DataSourceListener;

/**
 * @since 0.24
 */
public class JdbcTestModuleExtender extends ModuleExtender<JdbcTestModuleExtender> {

    public JdbcTestModuleExtender(Binder binder) {
        super(binder);
    }

    @Override
    public JdbcTestModuleExtender initAllExtensions() {
        contributeDataSourceListeners();
        return this;
    }

    public JdbcTestModuleExtender addDataSourceListener(DataSourceListener listener) {
        contributeDataSourceListeners().addBinding().toInstance(listener);
        return this;
    }

    public JdbcTestModuleExtender addDataSourceListener(Class<? extends DataSourceListener> listenerType) {
        contributeDataSourceListeners().addBinding().to(listenerType);
        return this;
    }

    private Multibinder<DataSourceListener> contributeDataSourceListeners() {
        return newSet(DataSourceListener.class);
    }
}
