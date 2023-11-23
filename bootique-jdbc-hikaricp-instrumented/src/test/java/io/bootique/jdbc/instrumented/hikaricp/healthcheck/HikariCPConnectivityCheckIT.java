/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.jdbc.instrumented.hikaricp.healthcheck;

import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import io.bootique.metrics.health.HealthCheckOutcome;
import io.bootique.metrics.health.HealthCheckRegistry;
import io.bootique.metrics.health.HealthCheckStatus;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class HikariCPConnectivityCheckIT {

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void healthChecks() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/healthcheck/HikariCPConnectivityCheckIT.yml")
                .autoLoadModules()
                .createRuntime();
        
        // trigger DataSource creation
        runtime.getInstance(DataSourceFactory.class).forName("db");

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);
        HealthCheckOutcome beforeShutdown = registry.runHealthCheck(HikariCPConnectivityCheck.healthCheckName("db"));
        assertEquals(HealthCheckStatus.OK, beforeShutdown.getStatus());

        shutdownDerby();

        HealthCheckOutcome afterShutdown = registry.runHealthCheck(HikariCPConnectivityCheck.healthCheckName("db"));
        assertEquals(HealthCheckStatus.CRITICAL, afterShutdown.getStatus());
    }

    private void shutdownDerby() {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            // the exception is actually expected on shutdown... go figure...
        }
    }
}
