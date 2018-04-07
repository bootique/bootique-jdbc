package io.bootique.jdbc.instrumented.hikaricp;

import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.inject.Injector;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jdbc.hikaricp.HikariCPManagedDataSourceFactory;
import io.bootique.jdbc.instrumented.hikaricp.healthcheck.HikariCPHealthCheckGroup;
import io.bootique.jdbc.instrumented.hikaricp.healthcheck.HikariCPHealthCheckGroupFactory;
import io.bootique.jdbc.managed.ManagedDataSourceSupplier;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@BQConfig("Pooling Hikari JDBC DataSource configuration with metrics.")
@JsonTypeName("hikari-instrumented")
public class HikariCPInstrumentedDataSourceFactory extends HikariCPManagedDataSourceFactory {

    public static final String METRIC_CATEGORY = "pool";
    public static final String METRIC_NAME_WAIT = "Wait";
    static final String METRIC_NAME_TOTAL_CONNECTIONS = "TotalConnections";
    static final String METRIC_NAME_IDLE_CONNECTIONS = "IdleConnections";
    static final String METRIC_NAME_ACTIVE_CONNECTIONS = "ActiveConnections";
    static final String METRIC_NAME_PENDING_CONNECTIONS = "PendingConnections";

    private HikariCPHealthCheckGroupFactory health;

    @Override
    public Optional<ManagedDataSourceSupplier> create(String dataSourceName, Injector injector) {

        if (getUrl() == null) {
            return Optional.empty();
        }

        Supplier<DataSource> startup = () -> {

            validate();

            HikariConfig hikariConfig = toConfiguration();
            HikariDataSource ds = new HikariDataSource(hikariConfig);

            this.addMetrics(ds, injector);
            this.addHealthChecks(ds, dataSourceName, injector);

            return ds;
        };

        Consumer<DataSource> shutdown = ds -> ((HikariDataSource) ds).close();

        return Optional.of(new ManagedDataSourceSupplier(getUrl(), startup, shutdown));
    }

    @BQConfigProperty
    public void setHealth(HikariCPHealthCheckGroupFactory health) {
        this.health = health;
    }

    private void addHealthChecks(HikariDataSource ds, String dataSourceName, Injector injector) {
        if (health != null) {
            HikariCPHealthCheckGroup group = injector.getInstance(HikariCPHealthCheckGroup.class);

            // TODO: we are mutating an injectable object here (HikariCPHealthCheckGroup).

            // The rest of the design ensures lazy initialization as confirmed by HikariCPInstrumentedModuleIT,
            // so HikariCPHealthCheckGroup is not consumed until fully initialized. Still dirty, but no easy
            // workaround.

            group.getHealthChecks().putAll(health.createHealthChecksMap(ds, dataSourceName, injector));
        }
    }

    private void addMetrics(HikariDataSource ds, Injector injector) {
        MetricRegistry registry = injector.getInstance(MetricRegistry.class);

        addWaitTimer(registry, ds);

        addTotalConnectionsGauge(registry, ds);
        addIdleConnectionsGauge(registry, ds);
        addActiveConnectionsGauge(registry, ds);
        addPendingConnectionsGauge(registry, ds);
    }


    /**
     * A {@link com.codahale.metrics.Timer} instance collecting how long requesting threads
     * to {@code getConnection()} are waiting for a connection (or timeout exception) from the pool.
     *
     * @param registry
     * @param ds
     */
    private void addWaitTimer(MetricRegistry registry, HikariDataSource ds) {
        registry.timer(MetricRegistry.name(ds.getPoolName(), METRIC_CATEGORY, METRIC_NAME_WAIT));
    }

    /**
     * A {@link CachedGauge}, refreshed on demand at 1 second resolution,
     * indicating the total number of connections in the pool.
     *
     * @param registry
     * @param ds
     */
    private void addTotalConnectionsGauge(MetricRegistry registry, HikariDataSource ds) {
        registry.register(MetricRegistry.name(ds.getPoolName(), METRIC_CATEGORY, METRIC_NAME_TOTAL_CONNECTIONS),
                (Gauge<Integer>) () -> ds.getHikariPoolMXBean().getTotalConnections());
    }

    /**
     * A {@link CachedGauge}, refreshed on demand at 1 second resolution,
     * indicating the number of idle connections in the pool.
     *
     * @param registry
     * @param ds
     */
    private void addIdleConnectionsGauge(MetricRegistry registry, HikariDataSource ds) {
        registry.register(MetricRegistry.name(ds.getPoolName(), METRIC_CATEGORY, METRIC_NAME_IDLE_CONNECTIONS),
                (Gauge<Integer>) () -> ds.getHikariPoolMXBean().getIdleConnections());
    }

    /**
     * A {@link CachedGauge}, refreshed on demand at 1 second resolution,
     * indicating the number of active (in-use) connections in the pool.
     *
     * @param registry
     * @param ds
     */
    private void addActiveConnectionsGauge(MetricRegistry registry, HikariDataSource ds) {
        registry.register(MetricRegistry.name(ds.getPoolName(), METRIC_CATEGORY, METRIC_NAME_ACTIVE_CONNECTIONS),
                (Gauge<Integer>) () -> ds.getHikariPoolMXBean().getActiveConnections());
    }

    /**
     * A {@link CachedGauge}, refreshed on demand at 1 second resolution,
     * indicating the number of threads awaiting connections from the pool.
     *
     * @param registry
     * @param ds
     */
    private void addPendingConnectionsGauge(MetricRegistry registry, HikariDataSource ds) {
        registry.register(MetricRegistry.name(ds.getPoolName(), METRIC_CATEGORY, METRIC_NAME_PENDING_CONNECTIONS),
                (Gauge<Integer>) () -> ds.getHikariPoolMXBean().getThreadsAwaitingConnection());

    }
}
