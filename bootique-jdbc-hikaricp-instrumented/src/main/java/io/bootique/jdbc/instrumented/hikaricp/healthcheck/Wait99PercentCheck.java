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

import io.bootique.jdbc.instrumented.hikaricp.JdbcHikariCPInstrumentedModule;
import io.bootique.metrics.health.check.ValueRange;
import io.bootique.metrics.health.check.ValueRangeCheck;
import io.bootique.value.Duration;

import java.util.function.Supplier;

/**
 * The health check checks that, on average, 99% of all calls to {@code getConnection()} obtain a
 * {@link java.sql.Connection} within a specified  number of milliseconds.
 *
 * @since 0.25
 */
public class Wait99PercentCheck extends ValueRangeCheck<Duration> {

    public Wait99PercentCheck(ValueRange<Duration> range, Supplier<Duration> valueSupplier) {
        super(range, valueSupplier);
    }

    /**
     * Generates stable qualified name for the "connection99Percent" check.
     *
     * @param dataSourceName Bootique configuration name of the data source being checked.
     * @return qualified name bq.jdbc.[dataSourceName].connection99Percent
     */
    public static String healthCheckName(String dataSourceName) {
        return JdbcHikariCPInstrumentedModule.METRIC_NAMING.name("Pool", dataSourceName, "Wait99Percent");
    }
}
