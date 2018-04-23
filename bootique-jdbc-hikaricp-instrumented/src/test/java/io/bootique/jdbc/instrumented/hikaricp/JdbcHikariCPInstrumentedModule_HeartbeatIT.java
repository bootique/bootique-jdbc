package io.bootique.jdbc.instrumented.hikaricp;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.instrumented.hikaricp.healthcheck.Connection99PercentCheck;
import io.bootique.jdbc.instrumented.hikaricp.healthcheck.ConnectivityCheck;
import io.bootique.metrics.health.HealthCheckModule;
import io.bootique.metrics.health.HealthCheckOutcome;
import io.bootique.metrics.health.HealthCheckStatus;
import io.bootique.metrics.health.heartbeat.Heartbeat;
import io.bootique.metrics.health.heartbeat.HeartbeatListener;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JdbcHikariCPInstrumentedModule_HeartbeatIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    // TODO: need to fix this!
    @Ignore("Fails on Travis")
    public void testHeartbeat() {

        HeartbeatTester tester = new HeartbeatTester();
        HeartbeatListener listener = tester::addResult;

        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/HikariCPInstrumentedModule_HeartbeatIT.yml")
                .autoLoadModules()
                .module(b -> BQCoreModule.extend(b).setDefaultCommand(TestHeartbeatCommand.class))
                .module(b -> HealthCheckModule.extend(b).addHeartbeatListener(listener))
                .createRuntime();

        runtime.run();

        // make sure we don't trigger DS creation by running heartbeat
        tester.assertNext(HealthCheckStatus.UNKNOWN);
        assertFalse(runtime.getInstance(DataSourceFactory.class).isStarted("db"));

        // trigger DataSource creation
        runtime.getInstance(DataSourceFactory.class).forName("db");
        tester.assertNext(HealthCheckStatus.OK);
    }

    static class HeartbeatTester {

        private volatile Map<String, HealthCheckOutcome> lastResult;
        private volatile CountDownLatch untilFirstHeartbeat = new CountDownLatch(1);

        void addResult(Map<String, HealthCheckOutcome> result) {
            this.lastResult = result;
            this.untilFirstHeartbeat.countDown();
        }

        void assertNext(HealthCheckStatus expectedStatus) {

            this.untilFirstHeartbeat = new CountDownLatch(1);

            try {
                assertTrue("No heartbeat", untilFirstHeartbeat.await(2, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                fail("interrupted: " + e.getMessage());
            }

            Map<String, HealthCheckOutcome> lastResult = this.lastResult;

            assertNotNull(lastResult);
            assertResult(expectedStatus, lastResult);
        }

        private void assertResult(HealthCheckStatus expectedStatus, Map<String, HealthCheckOutcome> result) {
            HealthCheckOutcome connectivity = result.get(ConnectivityCheck.healthCheckName("db"));
            HealthCheckOutcome connectivity99Pct = result.get(Connection99PercentCheck.healthCheckName("db"));

            assertNotNull("No connectivity check", connectivity);
            assertNotNull("No 99 connectivity percentile check", connectivity99Pct);
            assertEquals(2, result.size());

            assertEquals("Unexpected connectivity check result",expectedStatus, connectivity.getStatus());
            assertEquals("Unexpected 99 connectivity percentile check result",expectedStatus, connectivity99Pct.getStatus());
        }
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
