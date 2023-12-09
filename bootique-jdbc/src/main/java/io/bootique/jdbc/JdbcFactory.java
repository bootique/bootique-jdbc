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
package io.bootique.jdbc;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.bootique.annotation.BQConfig;
import io.bootique.jdbc.managed.ManagedDataSourceFactory;
import io.bootique.jdbc.managed.ManagedDataSourceStarter;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 3.0
 */
public class JdbcFactory {

    private final Map<String, ManagedDataSourceFactory> configs;

    @BQConfig("A map of DataSources by name")
    @JsonCreator
    public JdbcFactory(Map<String, ManagedDataSourceFactory> configs) {
        this.configs = configs;
    }

    // returning Starters map instead of DataSources, as it needs to be injectable independently for the sake of
    // instrumentation
    public Map<String, ManagedDataSourceStarter> create() {
        Map<String, ManagedDataSourceStarter> starters = new HashMap<>();
        configs.forEach((n, mdsf) -> starters.put(n, mdsf.create(n)));
        return starters;
    }
}
