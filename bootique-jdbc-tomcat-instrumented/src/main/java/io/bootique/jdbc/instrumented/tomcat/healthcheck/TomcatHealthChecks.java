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

import io.bootique.jdbc.DataSourceFactory;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.HealthCheckGroup;
import io.bootique.metrics.health.check.DeferredHealthCheck;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Generates individual healthchecks for a {@link io.bootique.jdbc.DataSourceFactory} DataSources. It is important to
 * report DataSource health individually.
 *
 * @since 1.0.RC1
 */
public class TomcatHealthChecks implements HealthCheckGroup {

    private DataSourceFactory dataSourceFactory;

    public TomcatHealthChecks(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    public Map<String, HealthCheck> getHealthChecks() {

        Map<String, HealthCheck> checks = new HashMap<>();

        dataSourceFactory.allNames().forEach(n ->
                checks.put(TomcatConnectivityCheck.healthCheckName(n), new DeferredHealthCheck(createConnectivityCheck(n)))
        );

        return checks;
    }

    private Supplier<Optional<HealthCheck>> createConnectivityCheck(String dataSourceName) {
        return () ->
                dataSourceFactory
                        .forNameIfStarted(dataSourceName)
                        .map(ds -> new TomcatConnectivityCheck(ds));
    }

}
