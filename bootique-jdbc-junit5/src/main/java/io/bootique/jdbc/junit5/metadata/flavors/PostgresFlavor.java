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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @since 2.0
 */
public class PostgresFlavor extends GenericFlavor {

    protected PostgresFlavor() {
    }

    public static PostgresFlavor create(DatabaseMetaData metaData) throws SQLException {
        PostgresFlavor flavor = new PostgresFlavor();
        flavor.supportsCatalogs = false;
        flavor.supportsSchemas = true;
        flavor.supportsParamsMetadata = true;
        flavor.supportsBatchUpdates = metaData.supportsBatchUpdates();
        flavor.identifierQuote = metaData.getIdentifierQuoteString();
        return flavor;
    }

    @Override
    public int columnType(int jdbcType, String nativeType) {

        switch (jdbcType) {
            case Types.TIMESTAMP:
                // PostgreSQL driver does not correctly detect Java 8 data types (TIMESTAMP with TZ, etc.)
                // TODO: pull request for Postgres?
                return "timestamptz".equals(nativeType) ? Types.TIMESTAMP_WITH_TIMEZONE : Types.TIMESTAMP;
            // TODO: the same hack for Types.TIME
            default:
                return super.columnType(jdbcType, nativeType);
        }
    }
}
