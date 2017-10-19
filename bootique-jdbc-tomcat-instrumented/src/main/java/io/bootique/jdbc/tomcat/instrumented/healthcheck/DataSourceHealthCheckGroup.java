package io.bootique.jdbc.tomcat.instrumented.healthcheck;

import io.bootique.jdbc.DataSourceFactory;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.HealthCheckGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates individual healthchecks for a {@link DataSourceFactory} DataSources. It is important to
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
