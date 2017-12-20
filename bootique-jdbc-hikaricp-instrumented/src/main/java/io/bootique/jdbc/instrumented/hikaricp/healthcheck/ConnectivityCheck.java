package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import com.zaxxer.hikari.HikariPoolMXBean;
import com.zaxxer.hikari.pool.HikariPool;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.HealthCheckOutcome;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * HikariCP "aliveness" standard check
 */
public class ConnectivityCheck implements HealthCheck {

    private final HikariPoolMXBean pool;
    private final long connectivityCheckTimeout;

    ConnectivityCheck(final HikariPoolMXBean pool, final long connectivityCheckTimeout) {
        this.pool = pool;
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

        try (Connection connection = ((HikariPool) pool).getConnection(connectivityCheckTimeout)) {
            return HealthCheckOutcome.ok();
        } catch (SQLException e) {
            return HealthCheckOutcome.critical(e);
        }
    }

    /**
     * Generates stable qualified name for the {@link ConnectivityCheck}
     *
     * @param dataSourceName
     * @return qualified name bq.jdbc.[dataSourceName].connectivity
     */
    public static String healthCheckName(String dataSourceName) {
        return "bq.jdbc." + dataSourceName + ".connectivity";
    }
}