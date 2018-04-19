package io.bootique.jdbc.instrumented.hikaricp;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariDataSource;
import io.bootique.jdbc.DataSourceListener;
import io.bootique.jdbc.instrumented.hikaricp.metrics.HikariMetricsBridge;

import javax.sql.DataSource;

/**
 * @since 0.26
 */
public class HikariCPMetricsInitializer implements DataSourceListener {

    private MetricRegistry metricRegistry;

    public HikariCPMetricsInitializer(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void beforeStartup(String name, String jdbcUrl) {
        // do nothing
    }

    @Override
    public void afterStartup(String name, String jdbcUrl, DataSource dataSource) {
        collectPoolMetrics(dataSource);
    }

    @Override
    public void afterShutdown(String name, String jdbcUrl, DataSource dataSource) {
        // do nothing
    }

    void collectPoolMetrics(DataSource dataSource) {
        HikariDataSource hikariDS = (HikariDataSource) dataSource;
        hikariDS.setMetricsTrackerFactory((pn, ps) -> new HikariMetricsBridge(pn, ps, metricRegistry));
    }
}
