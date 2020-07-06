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
import com.zaxxer.hikari.HikariDataSource;
import io.bootique.jdbc.DataSourceListener;
import io.bootique.jdbc.instrumented.hikaricp.metrics.HikariMetricsBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * @since 1.0.RC1
 */
public class HikariCPMetricsInitializer implements DataSourceListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HikariCPMetricsInitializer.class);

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
        if (dataSource instanceof HikariDataSource) {
            collectPoolMetrics((HikariDataSource) dataSource);
        } else {
            LOGGER.warn("DataSource ({}) is not an instance of HikariDataSource, skipping metrics init", dataSource.getClass());
        }
    }

    @Override
    public void afterShutdown(String name, String jdbcUrl, DataSource dataSource) {
        // do nothing
    }

    void collectPoolMetrics(HikariDataSource dataSource) {
        dataSource.setMetricsTrackerFactory((pn, ps) -> new HikariMetricsBridge(pn, ps, metricRegistry));
    }
}
