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
import io.bootique.BQRuntime;
import io.bootique.di.Key;
import io.bootique.di.TypeLiteral;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.DataSourceListener;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import io.bootique.metrics.health.HealthCheckRegistry;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class JdbcTomcatInstrumentedModuleIT {

    @BQTestTool
    static final BQTestFactory TEST_FACTORY = new BQTestFactory();

    @Test
    public void metricsListener_Injected() {
        BQRuntime runtime = TEST_FACTORY.app("-c", "classpath:io/bootique/jdbc/instrumented/dummy-ds.yml")
                .autoLoadModules()
                .createRuntime();

        TypeLiteral<Set<DataSourceListener>> typeLiteral = new TypeLiteral<Set<DataSourceListener>>() {
        };

        Set<DataSourceListener> set = runtime.getInstance(Key.get(typeLiteral));
        assertEquals(set.size(), 1);
        assertTrue(set.iterator().next() instanceof TomcatMetricsInitializer);
    }

    @Test
    public void metrics() {
        BQRuntime runtime = TEST_FACTORY.app("-c", "classpath:io/bootique/jdbc/instrumented/dummy-ds.yml")
                .autoLoadModules()
                .createRuntime();

        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);

        DataSource dataSource = factory.forName("db");
        assertNotNull(dataSource);

        MetricRegistry metricRegistry = runtime.getInstance(MetricRegistry.class);

        Set<String> expected = new HashSet<>(asList(
                "bq.JdbcTomcat.Pool.db.ActiveConnections",
                "bq.JdbcTomcat.Pool.db.IdleConnections",
                "bq.JdbcTomcat.Pool.db.PendingConnections",
                "bq.JdbcTomcat.Pool.db.Size"));

        assertEquals(expected, metricRegistry.getGauges().keySet());
    }

    @Test
    public void healthChecks() {
        BQRuntime runtime = TEST_FACTORY.app("-c", "classpath:io/bootique/jdbc/instrumented/dummy-ds.yml")
                .autoLoadModules()
                .createRuntime();

        Set<String> expectedNames = new HashSet<>(asList("bq.JdbcTomcat.Pool.db.Connectivity"));

        HealthCheckRegistry registry = runtime.getInstance(HealthCheckRegistry.class);
        assertEquals(expectedNames, registry.healthCheckNames());
    }
}
