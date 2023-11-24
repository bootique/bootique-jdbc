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

package io.bootique.jdbc.instrumented.tomcat.healthcheck;

import io.bootique.jdbc.instrumented.tomcat.JdbcTomcatInstrumentedModule;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.HealthCheckOutcome;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Checks DataSource health. Are connections available? Are they valid?
 *
 * @deprecated The alternative is switching to bootique-jdbc-hikaricp.
 */
@Deprecated(since = "3.0", forRemoval = true)
public class TomcatConnectivityCheck implements HealthCheck {

    private DataSource dataSource;

    public TomcatConnectivityCheck(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public HealthCheckOutcome check() throws Exception {

        try (Connection c = dataSource.getConnection()) {
            return c.isValid(1)
                    ? HealthCheckOutcome.ok()
                    : HealthCheckOutcome.critical("Connection validation failed");
        }
    }

    /**
     * Generates stable qualified name for the {@link TomcatConnectivityCheck}
     *
     * @param dataSourceName
     * @return qualified name bq.JdbcTomcat.Pool.[dataSourceName].Connectivity
     */
    public static String healthCheckName(String dataSourceName) {
        return JdbcTomcatInstrumentedModule.METRIC_NAMING.name("Pool", dataSourceName, "Connectivity");
    }
}
