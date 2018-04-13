package io.bootique.jdbc.instrumented.hikaricp.managed;

import io.bootique.jdbc.managed.ManagedDataSourceStarter;
import io.bootique.metrics.health.HealthCheckGroup;

import javax.sql.DataSource;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @since 0.26
 */
public class InstrumentedManagedDataSourceStarter extends ManagedDataSourceStarter {

    private HealthCheckGroup healthChecks;

    public InstrumentedManagedDataSourceStarter(
            String url,
            Supplier<DataSource> startup,
            Consumer<DataSource> shutdown,
            HealthCheckGroup healthChecks) {

        super(url, startup, shutdown);
        this.healthChecks = healthChecks;
    }

    public HealthCheckGroup getHealthChecks() {
        return healthChecks;
    }
}
