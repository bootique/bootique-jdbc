package io.bootique.jdbc.test;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import io.bootique.ModuleExtender;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.test.runtime.DataSourceListener;

import javax.sql.DataSource;
import java.util.Optional;


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
        contributeDataSourceListeners().addBinding().toInstance(new io.bootique.jdbc.DataSourceListener() {
            @Override
            public void beforeStartup(String name, String jdbcUrl) {
                listener.beforeStartup(name, Optional.ofNullable(jdbcUrl));
            }

            @Override
            public void afterStartup(String name, String jdbcUrl, DataSource dataSource) {
                listener.afterStartup(name, Optional.ofNullable(jdbcUrl), dataSource);
            }

            @Override
            public void afterShutdown(String name, String jdbcUrl, DataSource dataSource) {
                listener.afterShutdown(name, Optional.of(jdbcUrl));
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
        contributeDataSourceListeners().addBinding().toProvider(new DataSourceListenerProvider(listenerType));
        return this;
    }

    private Multibinder<io.bootique.jdbc.DataSourceListener> contributeDataSourceListeners() {
        return newSet(io.bootique.jdbc.DataSourceListener.class);
    }
}
