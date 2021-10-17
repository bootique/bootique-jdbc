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

package io.bootique.jdbc.instrumented.hikaricp;

import io.bootique.BQRuntime;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import io.bootique.metrics.health.HealthCheckRegistry;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class JdbcHikariCPInstrumentedModule_HealthChecksIT {

    @BQTestTool
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testHealthChecks() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/JdbcHikariCPInstrumentedModule_HealthChecksIT.yml")
                .autoLoadModules()
                .createRuntime();

        Set<String> expectedNames = new HashSet<>(asList(
                "bq.JdbcHikariCP.Pool.db.Connectivity",
                "bq.JdbcHikariCP.Pool.db.Wait99Percent"));

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);
        assertEquals(expectedNames, registry.healthCheckNames());
    }

    @Test
    public void testHealthChecksMultipleDs() {

        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/JdbcHikariCPInstrumentedModule_HealthChecksIT_multi.yml")
                .autoLoadModules()
                .createRuntime();

        Set<String> expectedNames = new HashSet<>(asList(
                "bq.JdbcHikariCP.Pool.db1.Connectivity",
                "bq.JdbcHikariCP.Pool.db1.Wait99Percent",
                "bq.JdbcHikariCP.Pool.db2.Connectivity",
                "bq.JdbcHikariCP.Pool.db2.Wait99Percent"));

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);
        assertEquals(expectedNames, registry.healthCheckNames());
    }

    @Test
    public void testHealthChecks_Implicit() {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/JdbcHikariCPInstrumentedModule_HealthChecksIT_no_health.yml")
                .autoLoadModules()
                .createRuntime();

        Set<String> expectedNames = new HashSet<>(asList(
                "bq.JdbcHikariCP.Pool.db.Connectivity",
                "bq.JdbcHikariCP.Pool.db.Wait99Percent"));

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);
        assertEquals(expectedNames, registry.healthCheckNames());
    }
}
