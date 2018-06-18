/**
 *  Licensed to ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.metrics.health.HealthCheckData;
import io.bootique.metrics.health.HealthCheckOutcome;
import io.bootique.metrics.health.HealthCheckRegistry;
import io.bootique.metrics.health.HealthCheckStatus;
import io.bootique.test.junit.BQTestFactory;
import io.bootique.value.Duration;
import org.junit.Rule;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Wait99PercentCheckIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testHealthChecks() throws SQLException {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/healthcheck/Wait99PercentCheckIT.yml")
                .autoLoadModules()
                .createRuntime();

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);
        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("db");

        HealthCheckOutcome outcome = registry.runHealthCheck(Wait99PercentCheck.healthCheckName("db"));
        assertEquals(HealthCheckStatus.OK, outcome.getStatus());
        HealthCheckData<Duration> data = (HealthCheckData<Duration>) outcome.getData().get();
        assertTrue(data.getValue().getDuration().toMillis() == 0);

        // checkout a few connections....
        for (int i = 0; i < 5; i++) {
            try (Connection c = ds.getConnection()) {
            }
        }

        outcome = registry.runHealthCheck(Wait99PercentCheck.healthCheckName("db"));
        assertEquals(outcome.getMessage(), HealthCheckStatus.OK, outcome.getStatus());

        data = (HealthCheckData<Duration>) outcome.getData().get();
        assertNotNull(data);

        // note that the underlying metric is rounded to milliseconds, so it is often == 0.
        // as a result we can't effectively test the metric payload...
        // assertTrue(data.getValue().getDuration().toMillis() > 0);

        // shutdown Derby
        shutdownDerby();

        outcome = registry.runHealthCheck(HikariCPConnectivityCheck.healthCheckName("db"));
        assertEquals(HealthCheckStatus.CRITICAL, outcome.getStatus());
    }

    private void shutdownDerby() {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            // the exception is actually expected on shutdown... go figure...
        }
    }
}
