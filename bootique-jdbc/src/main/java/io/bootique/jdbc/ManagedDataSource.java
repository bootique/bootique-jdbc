package io.bootique.jdbc;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @since 0.25
 */
public class ManagedDataSource {

    private String url;
    private Supplier<DataSource> startup;
    private Consumer<DataSource> shutdown;

    private DataSource dataSource;

    public ManagedDataSource(
            String url,
            Supplier<DataSource> startup,
            Consumer<DataSource> shutdown) {

        this.url = url;
        this.startup = startup;
        this.shutdown = shutdown;
    }

    public DataSource getDataSource() {
        return Objects.requireNonNull(dataSource, "DataSource hasn't been created yet. You need to call 'start' method.");
    }

    public String getUrl() {
        return url;
    }

    public DataSource start() {
        DataSource dataSource = startup.get();

        // TODO: check for race conditions?
        this.dataSource = dataSource;

        return dataSource;
    }

    public void shutdown() {
        shutdown.accept(dataSource);
    }
}
