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

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Provider;
import com.zaxxer.hikari.HikariDataSource;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.HealthCheckGroup;
import io.bootique.metrics.health.check.DeferredHealthCheck;
import io.bootique.metrics.health.check.DurationRangeFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@BQConfig("Configures health checks for a Hikari DataSource.")
public class HikariCPHealthChecksFactory {

    private DurationRangeFactory connectivity;
    private DurationRangeFactory connection99Percent;

    @BQConfigProperty("Configures a time threshold for DB connectivity check.")
    public void setConnectivity(DurationRangeFactory connectivity) {
        this.connectivity = connectivity;
    }

    @BQConfigProperty("Configures thresholds for a health check verifying 99th percentile for checkout times of pooled " +
            "connections.")
    public void setConnection99Percent(DurationRangeFactory connection99Percent) {
        this.connection99Percent = connection99Percent;
    }

    public HealthCheckGroup createHealthChecks(MetricRegistry registry,
                                               Provider<DataSourceFactory> dataSourceFactoryProvider,
                                               String dataSourceName) {

        Map<String, HealthCheck> checks = new HashMap<>(3);

        checks.put(HikariCPConnectivityCheck.healthCheckName(dataSourceName),
                new DeferredHealthCheck(createConnectivityCheck(dataSourceFactoryProvider, dataSourceName)));

        checks.put(Wait99PercentCheck.healthCheckName(dataSourceName),
                new DeferredHealthCheck(createConnection99PercentCheck(registry, dataSourceFactoryProvider, dataSourceName)));

        return () -> checks;
    }

    private Supplier<Optional<HealthCheck>> createConnectivityCheck(
            Provider<DataSourceFactory> dataSourceFactoryProvider,
            String dataSourceName) {

        return () ->
                dataSourceFactoryProvider
                        .get()
                        .forNameIfStarted(dataSourceName)
                        .map(ds -> (HikariDataSource) ds)
                        .map(ds -> new HikariCPConnectivityCheckFactory(connectivity).createHealthCheck(ds));
    }

    private Supplier<Optional<HealthCheck>> createConnection99PercentCheck(
            MetricRegistry registry,
            Provider<DataSourceFactory> dataSourceFactoryProvider,
            String dataSourceName) {

        return () ->
                dataSourceFactoryProvider
                        .get()
                        .forNameIfStarted(dataSourceName)
                        .map(ds -> (HikariDataSource) ds)
                        .map(ds -> new Wait99PercentCheckFactory(connection99Percent)
                                .createHealthCheck(registry, dataSourceName));
    }
}