package io.bootique.jdbc.test;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import io.bootique.jdbc.test.runtime.DataSourceListener;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Used to keep backward compatibility with 0.24 {@link JdbcTestModuleExtender#addDataSourceListener(Class)}
 *
 * @deprecated since 0.25
 */
public class DataSourceListenerProvider implements Provider<io.bootique.jdbc.DataSourceListener> {

    @Inject
    private Injector injector;

    private Class<? extends DataSourceListener> listenerType;

    public DataSourceListenerProvider(Class<? extends DataSourceListener> listenerType) {
        this.listenerType = listenerType;
    }

    @Override
    public io.bootique.jdbc.DataSourceListener get() {
        final DataSourceListener listener = injector.getInstance(listenerType);

        return new io.bootique.jdbc.DataSourceListener() {

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

        };
    }
}
