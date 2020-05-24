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
package io.bootique.jdbc.junit5.metadata.flavors;

/**
 * Provides DB type specific metadata and strategies for interacting with the database.
 *
 * @since 2.0
 */
public interface DbFlavor {

    String getIdentifierQuote();

    boolean supportsParamsMetadata();

    boolean supportsBatchUpdates();

    boolean supportsCatalogs();

    boolean supportsSchemas();

    int columnType(int jdbcType, String nativeType);

    default boolean shouldQuoteIdentifiers() {
        // per JDBC spec a space symboil means quotations ar enot supported
        return !" ".equals(getIdentifierQuote());
    }
}
