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

package io.bootique.jdbc.instrumented.tomcat;

import com.codahale.metrics.MetricRegistry;
import io.bootique.ModuleCrate;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.JdbcModule;
import io.bootique.jdbc.instrumented.tomcat.healthcheck.TomcatHealthChecks;
import io.bootique.metrics.MetricNaming;
import io.bootique.metrics.health.HealthCheckModule;

import javax.inject.Singleton;

/**
 * @deprecated The alternative is switching to bootique-jdbc-hikaricp.
 */
@Deprecated(since = "3.0", forRemoval = true)
public class JdbcTomcatInstrumentedModule implements BQModule {

    public static final MetricNaming METRIC_NAMING = MetricNaming.forModule(JdbcTomcatInstrumentedModule.class);

    @Override
    public ModuleCrate crate() {
        return ModuleCrate.of(this)
                .description("Deprecated, can be replaced with 'bootique-jdbc-hikaricp-instrumented'.")
                .build();
    }

    @Override
    public void configure(Binder binder) {
        JdbcModule.extend(binder).addDataSourceListener(TomcatMetricsInitializer.class);
        HealthCheckModule.extend(binder).addHealthCheckGroup(TomcatHealthChecks.class);
    }

    @Singleton
    @Provides
    TomcatMetricsInitializer provideMetricsListener(MetricRegistry metricRegistry) {
        return new TomcatMetricsInitializer(metricRegistry);
    }

    @Singleton
    @Provides
    TomcatHealthChecks provideDataSourceHealthCheckGroup(DataSourceFactory dataSourceFactory) {
        return new TomcatHealthChecks(dataSourceFactory);
    }
}
