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

package io.bootique.jdbc;

import javax.sql.DataSource;

/**
 * A listener attached to {@link DataSourceFactory} that is notified about DataSource start and stop events.
 *
 * @since 0.25
 */
public interface DataSourceListener {

    default void beforeStartup(String name, String jdbcUrl) {
    }

    default void afterStartup(String name, String jdbcUrl, DataSource dataSource) {
    }

    default void afterShutdown(String name, String jdbcUrl, DataSource dataSource) {
    }
}
