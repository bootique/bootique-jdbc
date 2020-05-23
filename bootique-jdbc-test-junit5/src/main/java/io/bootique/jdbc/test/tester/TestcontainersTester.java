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

import io.bootique.jdbc.test.DbTester;
import io.bootique.jdbc.test.datasource.DriverDataSource;
import org.junit.jupiter.api.Assertions;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * @since 2.0
 */
public class TestcontainersTester extends DbTester {

    private final String containerDbUrl;

    public TestcontainersTester(String containerDbUrl) {
        this.containerDbUrl = Objects.requireNonNull(containerDbUrl);
    }

    @Override
    protected DataSource createNonPoolingDataSource() {
        Assertions.assertDoesNotThrow(
                () -> Class.forName("org.testcontainers.jdbc.ContainerDatabaseDriver"),
                "Error loading testcontainers JDBC driver");
        return new DriverDataSource(null, containerDbUrl, null, null);
    }
}
