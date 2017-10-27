package io.bootique.jdbc.test;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import io.bootique.jdbc.test.runtime.DataSourceListener;

import javax.sql.DataSource;

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

        return new DataSourceListenerAdapter() {

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
        };
    }
}
