/**
 *    Licensed to the ObjectStyle LLC under one
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

package io.bootique.jdbc.instrumented.hikaricp;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariDataSource;
import io.bootique.jdbc.DataSourceListener;
import io.bootique.jdbc.instrumented.hikaricp.metrics.HikariMetricsBridge;

import javax.sql.DataSource;

/**
 * @since 0.26
 */
public class HikariCPMetricsInitializer implements DataSourceListener {

    private MetricRegistry metricRegistry;

    public HikariCPMetricsInitializer(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void beforeStartup(String name, String jdbcUrl) {
        // do nothing
    }

    @Override
    public void afterStartup(String name, String jdbcUrl, DataSource dataSource) {
        collectPoolMetrics(dataSource);
    }

    @Override
    public void afterShutdown(String name, String jdbcUrl, DataSource dataSource) {
        // do nothing
    }

    void collectPoolMetrics(DataSource dataSource) {
        HikariDataSource hikariDS = (HikariDataSource) dataSource;
        hikariDS.setMetricsTrackerFactory((pn, ps) -> new HikariMetricsBridge(pn, ps, metricRegistry));
    }
}
