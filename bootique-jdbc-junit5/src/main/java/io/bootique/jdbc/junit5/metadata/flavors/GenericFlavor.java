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

/**
 * @since 2.0
 */
public class GenericFlavor implements DbFlavor {

    protected boolean supportsCatalogs;
    protected boolean supportsSchemas;
    protected boolean supportsParamsMetadata;
    protected boolean supportsBatchUpdates;
    protected String identifierQuote;

    protected GenericFlavor() {
    }

    public static GenericFlavor create(DatabaseMetaData metaData) throws SQLException {
        GenericFlavor flavor = new GenericFlavor();
        flavor.supportsCatalogs = metaData.supportsCatalogsInTableDefinitions();
        flavor.supportsSchemas = metaData.supportsSchemasInTableDefinitions();
        flavor.supportsParamsMetadata = true;
        flavor.supportsBatchUpdates = metaData.supportsBatchUpdates();
        flavor.identifierQuote = metaData.getIdentifierQuoteString();

        return flavor;
    }

    @Override
    public String getIdentifierQuote() {
        return identifierQuote;
    }

    @Override
    public boolean supportsParamsMetadata() {
        return supportsParamsMetadata;
    }

    @Override
    public boolean supportsBatchUpdates() {
        return supportsBatchUpdates;
    }

    @Override
    public boolean supportsCatalogs() {
        return supportsCatalogs;
    }

    @Override
    public boolean supportsSchemas() {
        return supportsSchemas;
    }

    @Override
    public int columnType(int jdbcType, String nativeType) {
        return jdbcType;
    }
}
