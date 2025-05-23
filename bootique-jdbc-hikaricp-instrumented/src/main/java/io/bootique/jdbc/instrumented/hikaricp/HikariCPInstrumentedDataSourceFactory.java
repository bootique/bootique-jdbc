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
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.hikaricp.HikariCPManagedDataSourceFactory;
import io.bootique.jdbc.instrumented.hikaricp.healthcheck.HikariCPHealthChecksFactory;
import io.bootique.jdbc.instrumented.hikaricp.managed.InstrumentedManagedDataSourceStarter;
import io.bootique.jdbc.managed.ManagedDataSourceStarter;
import io.bootique.metrics.health.HealthCheckGroup;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import javax.sql.DataSource;
import java.util.function.Consumer;
import java.util.function.Supplier;

@BQConfig("Pooling Hikari JDBC DataSource configuration with metrics.")
@JsonTypeName("hikari-instrumented")
public class HikariCPInstrumentedDataSourceFactory extends HikariCPManagedDataSourceFactory {

    private final Provider<MetricRegistry> metricRegistry;
    private final Provider<DataSourceFactory> dataSourceFactory;

    private HikariCPHealthChecksFactory health;

    @Inject
    public HikariCPInstrumentedDataSourceFactory(
            Provider<MetricRegistry> metricRegistry,
            Provider<DataSourceFactory> dataSourceFactory) {
        this.metricRegistry = metricRegistry;
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    protected ManagedDataSourceStarter createDataSourceStarter(
            String dataSourceName,
            Supplier<DataSource> startup,
            Consumer<DataSource> shutdown) {

        HealthCheckGroup healthChecks = healthChecks(dataSourceName);
        return new InstrumentedManagedDataSourceStarter(this::getJdbcUrl, startup, shutdown, healthChecks);
    }

    @BQConfigProperty
    public void setHealth(HikariCPHealthChecksFactory health) {
        this.health = health;
    }

    private HealthCheckGroup healthChecks(String dataSourceName) {

        HikariCPHealthChecksFactory factory = this.health != null
                ? this.health
                : new HikariCPHealthChecksFactory(metricRegistry, dataSourceFactory);
        return factory.createHealthChecks(dataSourceName);
    }
}
