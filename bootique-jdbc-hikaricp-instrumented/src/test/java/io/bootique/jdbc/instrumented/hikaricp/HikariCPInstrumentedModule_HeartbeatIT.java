package io.bootique.jdbc.instrumented.hikaricp;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.instrumented.healthcheck.DataSourceHealthCheck;
import io.bootique.jdbc.instrumented.hikaricp.healthcheck.Connection99PercentCheck;
import io.bootique.jdbc.instrumented.hikaricp.healthcheck.ConnectivityCheck;
import io.bootique.metrics.health.HealthCheckModule;
import io.bootique.metrics.health.HealthCheckOutcome;
import io.bootique.metrics.health.HealthCheckStatus;
import io.bootique.metrics.health.heartbeat.Heartbeat;
import io.bootique.metrics.health.heartbeat.HeartbeatListener;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HikariCPInstrumentedModule_HeartbeatIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testHeartbeat() throws InterruptedException {

        CountDownLatch untilFirstHeartbeat = new CountDownLatch(1);

        Map<String, HealthCheckOutcome> result = new HashMap<>();

        HeartbeatListener listener = r -> {
            result.putAll(r);
            untilFirstHeartbeat.countDown();
        };

        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/HikariCPInstrumentedModule_HeartbeatIT.yml")
                .autoLoadModules()
                .module(b -> BQCoreModule.extend(b).setDefaultCommand(TestHeartbeatCommand.class))
                .module(b -> HealthCheckModule.extend(b).addHeartbeatListener(listener))
                .createRuntime();

        runtime.run();

        // trigger DataSource creation
        runtime.getInstance(DataSourceFactory.class).forName("db");

        assertTrue("No heartbeat", untilFirstHeartbeat.await(2, TimeUnit.SECONDS));

        HealthCheckOutcome hikariConnectivity = result.get(ConnectivityCheck.healthCheckName("db"));
        HealthCheckOutcome hikari99Pct = result.get(Connection99PercentCheck.healthCheckName("db"));
        HealthCheckOutcome commonConnectivity = result.get(DataSourceHealthCheck.healthCheckName("db"));

        assertNotNull("No common connectivity check", commonConnectivity);
        assertEquals(HealthCheckStatus.OK, commonConnectivity.getStatus());

        assertNotNull("No Hikari connectivity check", hikariConnectivity);
        assertEquals(HealthCheckStatus.OK, hikariConnectivity.getStatus());

        assertNotNull("No Hikari 99 connectivity percentile check", hikari99Pct);
        assertEquals(HealthCheckStatus.OK, hikari99Pct.getStatus());

        assertEquals(3, result.size());
    }

    static class TestHeartbeatCommand implements Command {
        private Provider<Heartbeat> heartbeatProvider;

        @Inject
        public TestHeartbeatCommand(Provider<Heartbeat> heartbeatProvider) {
            this.heartbeatProvider = heartbeatProvider;
        }

        @Override
        public CommandOutcome run(Cli cli) {
            heartbeatProvider.get().start();
            return CommandOutcome.succeededAndForkedToBackground();
        }
    }
}
