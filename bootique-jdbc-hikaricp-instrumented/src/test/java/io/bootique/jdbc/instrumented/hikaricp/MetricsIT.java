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

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.instrumented.hikaricp.metrics.HikariMetricsBridge;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class MetricsIT {

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testConnectionStateMetrics() throws SQLException {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/MetricsIT.yml")
                .autoLoadModules()
                .createRuntime();

        String dsName = "db";
        MetricRegistry registry = runtime.getInstance(MetricRegistry.class);
        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName(dsName);

        // wait for the metrics to be initialized...
        Gauge<Integer> activeConnections = registry.getGauges().get(HikariMetricsBridge.activeConnectionsMetric(dsName));

        assertNotNull(activeConnections, registry.getGauges().keySet() + "");
        assertEquals(Integer.valueOf(0), activeConnections.getValue());

        for (int i = 0; i < 3; i++) {
            try (Connection c = ds.getConnection()) {
                // the metric does not refresh immediately, so need to test with a delay...
                await("recorded_active_connection")
                        .atMost(1200, TimeUnit.MILLISECONDS)
                        .until(() -> activeConnections.getValue(), equalTo(1));
            }
        }

        await("no_more_active_connections")
                .atMost(1200, TimeUnit.MILLISECONDS).until(() -> activeConnections.getValue(),
                equalTo(0));
    }


    @Test
    public void testUsageMetric() throws SQLException, InterruptedException {

        BQRuntime runtime = testFactory
                .app("-c", "classpath:io/bootique/jdbc/instrumented/hikaricp/MetricsIT.yml")
                .autoLoadModules()
                .createRuntime();

        String dsName = "db";

        MetricRegistry registry = runtime.getInstance(MetricRegistry.class);
        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName(dsName);

        Histogram usage = registry.getHistograms().get(HikariMetricsBridge.connectionUsageMetric(dsName));
        assertNotNull(usage);
        assertEquals(0., usage.getSnapshot().get99thPercentile(), 0.001);

        for (int i = 0; i < 10; i++) {
            try (Connection c = ds.getConnection()) {
                // checkout some connections to generate usage stats ... make sure usage is > 0
                Thread.sleep(2);
            }
        }

        await("usage")
                .atMost(1200, TimeUnit.MILLISECONDS)
                .until(() -> usage.getSnapshot().getMax() > 0);
    }

    // TODO: we can't effectively check "wait" metric? Checkout time is normally < 1ms and Hikari uses millisecond clock on MacOS.
}
