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
import java.util.Collection;
import java.util.Optional;

public interface DataSourceFactory {

    /**
     * Returns a DataSource for the specified name. Depending on implementation the first call for a given name
     * may trigger DataSource creation.
     *
     * @param dataSourceName symbolic name of the DataSource we are interested in.
     * @return a DataSource for a given name.
     */
    DataSource forName(String dataSourceName);

    /**
     * Returns the names of all configured DataSources. Each of these names can
     * be used as an argument to {@link #forName(String)} method.
     *
     * @return the names of all known DataSources.
     * @since 0.6
     */
    Collection<String> allNames();

    /**
     * Returns true if the named DataSource has already been created. This allows to inspect lazy DataSourceFactory's
     * without triggering immediate DataSource creation.
     *
     * @param dataSourceName symbolic name of the DataSource we are interested in.
     * @return true if the named DataSource has already been created.
     * @since 0.26
     */
    boolean isStarted(String dataSourceName);

    /**
     * @param dataSourceName symbolic name of the DataSource we are interested in.
     * @return an optional DataSource for a given name. Will be an empty Optional if the DataSource for that name is
     * not yet started.
     * @since 0.26
     */
    default Optional<DataSource> forNameIfStarted(String dataSourceName) {
        return isStarted(dataSourceName) ? Optional.of(forName(dataSourceName)) : Optional.empty();
    }
}
