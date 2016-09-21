package io.bootique.jdbc.instrumented.healthcheck;

import com.codahale.metrics.health.HealthCheck;

import javax.sql.DataSource;

/**
 * Does a healthcheck on the DataSource.
 *
 * @since 0.12
 */
public class DataSourceHealthcheck extends HealthCheck {

    private DataSource dataSource;

    public DataSourceHealthcheck(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected Result check() throws Exception {
        return null;
    }
}
