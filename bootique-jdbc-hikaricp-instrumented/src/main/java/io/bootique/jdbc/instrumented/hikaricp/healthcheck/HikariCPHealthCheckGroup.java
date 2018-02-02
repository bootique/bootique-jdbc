package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.HealthCheckGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * A container of HikariCP-specific health checks.
 */
public class HikariCPHealthCheckGroup implements HealthCheckGroup {
    private Map<String, HealthCheck> healthChecks;

    public HikariCPHealthCheckGroup() {
        this.healthChecks = new HashMap<>();
    }

    @Override
    public Map<String, HealthCheck> getHealthChecks() {
        return healthChecks;
    }
}
