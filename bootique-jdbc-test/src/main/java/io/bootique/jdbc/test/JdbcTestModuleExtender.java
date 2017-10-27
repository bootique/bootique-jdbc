package io.bootique.jdbc.test;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import io.bootique.ModuleExtender;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.test.runtime.DataSourceListener;

import javax.sql.DataSource;


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

    /**
     * @param listener attached to {@link DataSourceFactory}
     * @return this extender
     * @deprecated since 0.25 in favor of {@link io.bootique.jdbc.JdbcModuleExtender#addDataSourceListener(io.bootique.jdbc.DataSourceListener)}
     */
    public JdbcTestModuleExtender addDataSourceListener(DataSourceListener listener) {
        contributeDataSourceListeners().addBinding().toInstance(new DataSourceListenerAdapter() {
            @Override
            public void beforeStartup(String name, String jdbcUrl) {
                listener.beforeStartup(name, jdbcUrl);
            }

            @Override
            public void afterStartup(String name, String jdbcUrl, DataSource dataSource) {
                listener.afterStartup(name, jdbcUrl, dataSource);
            }

            @Override
            public void afterShutdown(String name, String jdbcUrl, DataSource dataSource) {
                listener.afterShutdown(name, jdbcUrl);
            }

            @Override
            public void afterShutdown(String name, String jdbcUrl) {
                listener.afterShutdown(name, jdbcUrl);
            }
        });
        return this;
    }

    /**
     * @param listenerType type of listener attached to {@link DataSourceFactory}
     * @return this extender
     * @deprecated since 0.25 favor of {@link io.bootique.jdbc.JdbcModuleExtender#addDataSourceListener(Class)}
     */
    public JdbcTestModuleExtender addDataSourceListener(Class<? extends DataSourceListener> listenerType) {
        contributeDataSourceListeners().addBinding().toProvider(DataSourceListenerProvider.class);
//        contributeDataSourceListeners().addBinding().to(listenerType);

        return this;
    }

    private Multibinder<io.bootique.jdbc.DataSourceListener> contributeDataSourceListeners() {
        return newSet(io.bootique.jdbc.DataSourceListener.class);
    }
}
