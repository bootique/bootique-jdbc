package io.bootique.jdbc.managed;

import javax.sql.DataSource;
import java.util.function.Consumer;

/**
 * A DataSource wrapper with provider-specific strategies to read DataSource metadata and shut down the
 * DataSource.
 *
 * @since 0.25
 */
public class ManagedDataSource {

    private String url;
    private DataSource dataSource;
    private Consumer<DataSource> shutdownHandler;

    public ManagedDataSource(String url, DataSource dataSource, Consumer<DataSource> shutdownHandler) {
        this.url = url;
        this.dataSource = dataSource;
        this.shutdownHandler = shutdownHandler;
    }

    public String getUrl() {
        return url;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void shutdown() {
        shutdownHandler.accept(dataSource);
    }
}
