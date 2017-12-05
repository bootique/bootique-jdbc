package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.HealthCheckOutcome;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * HikariCP "aliveness" standard check
 */
public class ConnectivityCheck implements HealthCheck {

    private final DataSourceFactory dataSourceFactory;
    private final String dataSourceName;

    private final long connectivityCheckTimeout;

    ConnectivityCheck(final DataSourceFactory dataSourceFactory, final String dataSourceName, final long connectivityCheckTimeout) {
        this.dataSourceFactory = dataSourceFactory;
        this.dataSourceName = dataSourceName;
        this.connectivityCheckTimeout = (connectivityCheckTimeout > 0 && connectivityCheckTimeout != Integer.MAX_VALUE ? connectivityCheckTimeout : TimeUnit.SECONDS.toMillis(10));
    }

    /**
     * The health check obtains a {@link Connection} from the pool and immediately return it.
     * The standard HikariCP internal "aliveness" check will be run.
     *
     * @return {@link HealthCheckOutcome#ok()} if an "alive" {@link Connection} can be obtained,
     * otherwise {@link HealthCheckOutcome#critical()} if the connection fails or times out
     * @throws Exception {@inheritDoc}
     */
    @Override
    public HealthCheckOutcome check() throws Exception {
        HikariDataSource ds = (HikariDataSource) dataSourceFactory.forName(dataSourceName);

        try (Connection connection = ((HikariPool) ds.getHikariPoolMXBean()).getConnection(connectivityCheckTimeout)) {
            return HealthCheckOutcome.ok();
        } catch (SQLException e) {
            return HealthCheckOutcome.critical(e);
        }
    }
}