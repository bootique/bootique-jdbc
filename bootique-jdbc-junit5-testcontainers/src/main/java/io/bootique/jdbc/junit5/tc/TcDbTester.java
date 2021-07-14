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
package io.bootique.jdbc.junit5.tc;

import io.bootique.jdbc.junit5.DbTester;
import io.bootique.jdbc.junit5.datasource.DriverDataSource;
import io.bootique.junit5.BQTestScope;
import org.testcontainers.containers.JdbcDatabaseContainer;

/**
 * A DbTester based on the Testcontainers library.
 *
 * @since 2.0
 */
public abstract class TcDbTester extends DbTester<TcDbTester> {

    /**
     * Creates a tester that will bootstrap a DB using Docker/Testcontainers. If the tester is executed in the "global"
     * scope (per {@link io.bootique.junit5.BQTestTool} annotation), the tester will alter "containerDbUrl" internally
     * before passing it to Testcontainers, forcing a "TC_REUSABLE=true" parameter regardless of its presence
     * or value in the original String.
     *
     * @param containerDbUrl a Testcontainers DB URL
     * @return a new tester instance
     * @see <a href="https://www.testcontainers.org/modules/databases/jdbc/">Testcontainers JDBC URLs</a>
     */
    public static TcDbTester db(String containerDbUrl) {
        return new UrlTcDbTester(containerDbUrl);
    }

    public static TcDbTester db(JdbcDatabaseContainer container) {
        return new ContainerTcDbTester(container, null, null);
    }

    public static TcDbTester db(JdbcDatabaseContainer container, String userName, String password) {
        return new ContainerTcDbTester(container, userName, password);
    }

    @Override
    protected DriverDataSource createNonPoolingDataSource(BQTestScope scope) {
        return new DriverDataSource(null, dbUrl(scope), dbUser(), dbPassword());
    }

    protected abstract String dbUser();

    protected abstract String dbPassword();

    protected abstract String dbUrl(BQTestScope scope);
}
