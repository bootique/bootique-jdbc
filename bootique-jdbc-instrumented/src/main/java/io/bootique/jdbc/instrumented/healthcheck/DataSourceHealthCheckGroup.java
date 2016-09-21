package io.bootique.jdbc.instrumented.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.metrics.healthcheck.HealthCheckGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates individual healthchecks for a {@link io.bootique.jdbc.DataSourceFactory} DataSources. It is important to
 * report DataSource health individually.
 *
 * @since 0.12
 */
public class DataSourceHealthCheckGroup implements HealthCheckGroup {

    private DataSourceFactory dataSourceFactory;

    public DataSourceHealthCheckGroup(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    public Map<String, HealthCheck> getHealthChecks() {

        Map<String, HealthCheck> checks = new HashMap<>();

        // TODO: namespacing module healthchecks..
        dataSourceFactory.allNames().forEach(n -> checks.put(n, new DataSourceHealthCheck(dataSourceFactory, n)));

        return checks;
    }
}
