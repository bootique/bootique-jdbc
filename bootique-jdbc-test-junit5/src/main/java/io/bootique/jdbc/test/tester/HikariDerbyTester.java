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
import io.bootique.jdbc.test.JdbcTester;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since 2.0
 */
public class HikariDerbyTester extends JdbcTester {

    private final AtomicInteger dbId;
    private final File baseDirectory;

    public HikariDerbyTester(File baseDirectory) {
        this.baseDirectory = Objects.requireNonNull(baseDirectory);
        this.dbId = new AtomicInteger(0);
    }

    @Override
    protected void configureBootiqueDataSource(Binder binder, String dataSourceName) {
        DataSourcePropertyBuilder.create(binder, dataSourceName)
                .property("type", "hikari")
                .property("jdbcUrl", jdbcUrl(dataSourceName));
    }

    protected String jdbcUrl(String dataSourceName) {
        return String.format("jdbc:derby:%s/%s_%s;create=true", baseDirectory, dataSourceName, dbId.getAndIncrement());
    }
}
