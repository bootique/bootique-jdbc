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

import com.codahale.metrics.MetricRegistry;
import io.bootique.BQModuleProvider;
import io.bootique.ModuleCrate;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.jdbc.JdbcModule;
import io.bootique.jdbc.hikaricp.JdbcHikariCPModule;
import io.bootique.jdbc.instrumented.hikaricp.healthcheck.HikariCPHealthChecks;
import io.bootique.jdbc.managed.ManagedDataSourceStarter;
import io.bootique.metrics.MetricNaming;
import io.bootique.metrics.MetricsModule;
import io.bootique.metrics.health.HealthCheckModule;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;

import static java.util.Arrays.asList;

public class JdbcHikariCPInstrumentedModule implements BQModule, BQModuleProvider {

    public static final MetricNaming METRIC_NAMING = MetricNaming.forModule(JdbcHikariCPInstrumentedModule.class);

    @Override
    public ModuleCrate moduleCrate() {
        return ModuleCrate.of(this)
                .provider(this)
                .description("Integrates metrics for HikariCP JDBC DataSource")
                .build();
    }

    @Override
    @Deprecated(since = "3.0", forRemoval = true)
    public Collection<BQModuleProvider> dependencies() {
        return asList(
                new JdbcHikariCPModule(),
                new MetricsModule(),
                new HealthCheckModule()
        );
    }

    @Override
    public void configure(Binder binder) {
        JdbcModule.extend(binder).addFactoryType(HikariCPInstrumentedDataSourceFactory.class);
        JdbcModule.extend(binder).addDataSourceListener(HikariCPMetricsInitializer.class);
        HealthCheckModule.extend(binder).addHealthCheckGroup(HikariCPHealthChecks.class);
    }

    @Singleton
    @Provides
    HikariCPHealthChecks provideHealthCheckGroup(Map<String, ManagedDataSourceStarter> starters) {
        return new HikariCPHealthChecks(starters);
    }

    @Singleton
    @Provides
    HikariCPMetricsInitializer provideMetricsInitializer(MetricRegistry metricRegistry) {
        return new HikariCPMetricsInitializer(metricRegistry);
    }
}
