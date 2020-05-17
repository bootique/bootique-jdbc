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

import io.bootique.BQCoreModule;
import io.bootique.di.Binder;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since 2.0
 */
public class HikariDerbyConfig implements TestDataSourceConfig {

    private final AtomicInteger dataSourceId;
    private final File baseDirectory;

    public HikariDerbyConfig(File baseDirectory) {
        this.baseDirectory = Objects.requireNonNull(baseDirectory);
        this.dataSourceId = new AtomicInteger(0);
    }

    @Override
    public void configure(Binder binder, String dataSourceName) {
        BQCoreModule.extend(binder)
                .setProperty(jdbcProperty(dataSourceName, "type"), "hikari")
                .setProperty(jdbcProperty(dataSourceName, "jdbcUrl"), jdbcUrl(dataSourceName));
    }

    protected String jdbcUrl(String dataSourceName) {
        return String.format("jdbc:derby:%s/%s_%s;create=true", baseDirectory, dataSourceName, dataSourceId.getAndIncrement());
    }
}
