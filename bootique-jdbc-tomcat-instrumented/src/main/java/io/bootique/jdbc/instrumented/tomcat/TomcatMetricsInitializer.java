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

package io.bootique.jdbc.instrumented.tomcat;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import io.bootique.jdbc.DataSourceListener;
import org.apache.tomcat.jdbc.pool.ConnectionPool;

import javax.sql.DataSource;

/**
 * @since 0.25
 */
public class TomcatMetricsInitializer implements DataSourceListener {

    private MetricRegistry metricRegistry;

    public TomcatMetricsInitializer(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void beforeStartup(String name, String jdbcUrl) {
        // do nothing
    }

    @Override
    public void afterStartup(String name, String jdbcUrl, DataSource dataSource) {
        collectPoolMetrics(name, dataSource);
    }

    @Override
    public void afterShutdown(String name, String jdbcUrl, DataSource dataSource) {
        // do nothing
    }

    void collectPoolMetrics(String name, DataSource dataSource) {
        org.apache.tomcat.jdbc.pool.DataSource tomcat = (org.apache.tomcat.jdbc.pool.DataSource) dataSource;
        ConnectionPool pool = tomcat.getPool();


        metricRegistry.register(JdbcTomcatInstrumentedModule.METRIC_NAMING.name("Pool", name, "ActiveConnections"),
                (Gauge<Integer>) () -> pool.getActive());
        metricRegistry.register(JdbcTomcatInstrumentedModule.METRIC_NAMING.name("Pool", name, "IdleConnections"),
                (Gauge<Integer>) () -> pool.getIdle());
        metricRegistry.register(JdbcTomcatInstrumentedModule.METRIC_NAMING.name("Pool", name, "PendingConnections"),
                (Gauge<Integer>) () -> pool.getWaitCount());
        metricRegistry.register(JdbcTomcatInstrumentedModule.METRIC_NAMING.name("Pool", name, "Size"),
                (Gauge<Integer>) () -> pool.getSize());
    }
}
