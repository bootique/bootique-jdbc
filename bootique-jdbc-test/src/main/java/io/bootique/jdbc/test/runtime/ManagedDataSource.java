package io.bootique.jdbc.test.runtime;

import javax.sql.DataSource;

/**
 * @since 0.12
 */
class ManagedDataSource {

    private DataSource dataSource;
    private String url;

    ManagedDataSource(DataSource dataSource, String url) {
        this.dataSource = dataSource;
        this.url = url;
    }

    DataSource getDataSource() {
        return dataSource;
    }

    String getUrl() {
        return url;
    }
}
