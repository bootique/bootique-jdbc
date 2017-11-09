package io.bootique.jdbc;

import javax.sql.DataSource;
import java.util.function.Consumer;

/**
 * @since 0.25
 */
public class ManagedDataSource {
    private javax.sql.DataSource dataSource;
    private String url;
    private Consumer<javax.sql.DataSource> shutdown;

    public ManagedDataSource(DataSource dataSource, String url, Consumer<javax.sql.DataSource> shutdown) {
        this.dataSource = dataSource;
        this.url = url;
        this.shutdown = shutdown;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public String getUrl() {
        return url;
    }

    public void shutdown() {
        shutdown.accept(dataSource);
    }
}
