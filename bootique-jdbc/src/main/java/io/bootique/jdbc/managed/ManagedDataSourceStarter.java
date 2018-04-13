package io.bootique.jdbc.managed;

import javax.sql.DataSource;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An object that can create and start a {@link ManagedDataSource} on demand. Also provides access to common DataSource
 * metadata, namely its JDBC URL.
 *
 * @since 0.25
 */
public class ManagedDataSourceStarter {

    private String url;
    private Supplier<DataSource> startup;
    private Consumer<DataSource> shutdown;

    public ManagedDataSourceStarter(
            String url,
            Supplier<DataSource> startup,
            Consumer<DataSource> shutdown) {

        this.url = url;
        this.startup = startup;
        this.shutdown = shutdown;
    }

    public String getUrl() {
        return url;
    }

    public ManagedDataSource start() {
        DataSource dataSource = startup.get();
        return new ManagedDataSource(url, dataSource, shutdown);
    }
}
