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

package io.bootique.jdbc.managed;

import javax.sql.DataSource;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A DataSource wrapper with provider-specific strategies to read DataSource metadata and shut down the
 * DataSource.
 */
public class ManagedDataSource {

    private final Supplier<String> url;
    private final DataSource dataSource;
    private final Consumer<DataSource> shutdownHandler;

    public ManagedDataSource(Supplier<String> url, DataSource dataSource, Consumer<DataSource> shutdownHandler) {
        this.url = url;
        this.dataSource = dataSource;
        this.shutdownHandler = shutdownHandler;
    }

    public String getUrl() {
        return url.get();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void shutdown() {
        shutdownHandler.accept(dataSource);
    }
}
