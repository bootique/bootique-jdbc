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
package io.bootique.jdbc.junit5.metadata;

import java.util.Objects;

/**
 * DB table fully qualified name that may contain table name, plus optional catalog and schema.
 *
 * @since 2.0
 */
public record TableFQName(String catalog, String schema, String table) {

    public TableFQName(String catalog, String schema, String table) {
        this.catalog = catalog != null && catalog.isEmpty() ? null : catalog;
        this.schema = schema != null && schema.isEmpty() ? null : schema;
        this.table = Objects.requireNonNull(table);
    }

    public static TableFQName forName(String tableName) {
        return new TableFQName(null, null, tableName);
    }

    public static TableFQName forSchemaAndName(String schema, String tableName) {
        return new TableFQName(null, schema, tableName);
    }

    public static TableFQName forCatalogAndName(String catalog, String tableName) {
        return new TableFQName(catalog, null, tableName);
    }

    public static TableFQName forCatalogSchemaAndName(String catalog, String schema, String tableName) {
        return new TableFQName(catalog, schema, tableName);
    }

    public boolean hasCatalog() {
        return catalog != null;
    }

    public boolean hasSchema() {
        return schema != null;
    }

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();
        if (catalog != null) {
            buffer.append(catalog).append(".");
        }

        if (schema != null) {
            buffer.append(schema).append(".");
        }

        buffer.append(table);
        return buffer.toString();
    }
}
