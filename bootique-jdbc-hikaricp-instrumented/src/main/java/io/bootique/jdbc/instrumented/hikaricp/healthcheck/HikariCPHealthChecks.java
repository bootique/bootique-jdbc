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

import io.bootique.jdbc.instrumented.hikaricp.managed.InstrumentedManagedDataSourceStarter;
import io.bootique.jdbc.managed.ManagedDataSourceStarter;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.HealthCheckGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * A container of HikariCP-specific health checks. Delegates health check creation to the underlying set of
 * {@link ManagedDataSourceStarter}'s.
 */
public class HikariCPHealthChecks implements HealthCheckGroup {

    private final Map<String, ManagedDataSourceStarter> starters;

    public HikariCPHealthChecks(Map<String, ManagedDataSourceStarter> starters) {
        this.starters = starters;
    }

    @Override
    public Map<String, HealthCheck> getHealthChecks() {

        Map<String, HealthCheck> checks = new HashMap<>();

        starters.forEach((b, s) -> {

            // extract health checks from Hikari instrumented starters
            if (s instanceof InstrumentedManagedDataSourceStarter is) {
                checks.putAll(is.getHealthChecks().getHealthChecks());
            }
        });

        return checks;
    }
}
