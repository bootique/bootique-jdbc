package io.bootique.jdbc.test.runtime;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * @since 0.12
 */
class ManagedDataSource {

    private DataSource dataSource;
    private Optional<String> url;

    ManagedDataSource(DataSource dataSource, Optional<String> url) {
        this.dataSource = dataSource;
        this.url = url;
    }

    DataSource getDataSource() {
        return dataSource;
    }

    Optional<String> getUrl() {
        return url;
    }
}
