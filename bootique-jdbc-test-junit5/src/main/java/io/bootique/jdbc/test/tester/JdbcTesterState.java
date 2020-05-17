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
package io.bootique.jdbc.test.tester;

import io.bootique.di.Binder;
import io.bootique.di.Injector;
import io.bootique.di.Key;
import io.bootique.jdbc.test.JdbcTester;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @since 2.0
 */
public class JdbcTesterState {

    private static final ExtensionContext.Namespace JUNIT_NAMESPACE = ExtensionContext.Namespace.create(JdbcTester.class);
    private static final String JUNIT_KEY = "DataSource";

    private static final Key<ExtensionContext.Store> BQ_STORE_KEY = Key.get(ExtensionContext.Store.class, TestDataSourceFactory.class.getName());

    public static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(JUNIT_NAMESPACE);
    }

    public static ExtensionContext.Store saveDataSource(ExtensionContext context, DataSource dataSource) {
        ExtensionContext.Store store = getStore(context);

        // TODO: how do we support multiple DataSources per test?
        store.put(JUNIT_KEY, dataSource);
        return store;
    }

    public static void bindToBootique(Binder binder, ExtensionContext.Store dataSourceStore) {
        binder.bind(BQ_STORE_KEY).toInstance(dataSourceStore);
    }

    public static DataSource getDataSource(Injector injector) {
        assertTrue(injector.hasProvider(BQ_STORE_KEY), "JUnit5 Store with test DataSource is not bootstrapped in the test Bootique app");
        ExtensionContext.Store store = injector.getInstance(BQ_STORE_KEY);

        DataSource dataSource = store.get(JUNIT_KEY, DataSource.class);
        assertNotNull(dataSource, "No test-managed DataSource is available in the test environment");
        return dataSource;
    }
}
