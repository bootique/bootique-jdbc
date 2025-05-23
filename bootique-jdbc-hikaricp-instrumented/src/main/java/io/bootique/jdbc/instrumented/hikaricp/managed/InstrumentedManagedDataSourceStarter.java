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

package io.bootique.jdbc.instrumented.hikaricp.managed;

import io.bootique.jdbc.managed.ManagedDataSourceStarter;
import io.bootique.metrics.health.HealthCheckGroup;

import javax.sql.DataSource;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InstrumentedManagedDataSourceStarter extends ManagedDataSourceStarter {

    private final HealthCheckGroup healthChecks;

    public InstrumentedManagedDataSourceStarter(
            Supplier<String> url,
            Supplier<DataSource> startup,
            Consumer<DataSource> shutdown,
            HealthCheckGroup healthChecks) {

        super(url, startup, shutdown);
        this.healthChecks = healthChecks;
    }

    public HealthCheckGroup getHealthChecks() {
        return healthChecks;
    }
}
