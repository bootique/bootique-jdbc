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

import io.bootique.jdbc.junit5.metadata.flavors.DbFlavor;
import io.bootique.jdbc.junit5.metadata.flavors.DbFlavorFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 2.0
 */
public class DbMetadata {

    private final DataSource dataSource;
    private final DbFlavor flavor;
    private final Map<TableFQName, DbTableMetadata> tables;

    protected DbMetadata(DataSource dataSource, DbFlavor flavor) {

        // note that we can't cache DatabaseMetaData, as it stops working once the underlying connection is closed,
        // so keeping the DataSource around to get it back whenever we need to compile table info, etc.

        this.dataSource = dataSource;
        this.flavor = Objects.requireNonNull(flavor);
        this.tables = new ConcurrentHashMap<>();
    }

    public static DbMetadata create(DataSource dataSource) {
        DbFlavor flavor = DbFlavorFactory.create(dataSource);
        return new DbMetadata(dataSource, flavor);
    }

    /**
     * @since 4.0
     */
    public static DbMetadata create(DataSource dataSource, DbFlavor flavor) {
        return new DbMetadata(dataSource, flavor);
    }

    public DbFlavor getFlavor() {
        return flavor;
    }

    public String getIdentifierQuote() {
        return flavor.getIdentifierQuote();
    }

    public boolean shouldQuoteIdentifiers() {
        return flavor.shouldQuoteIdentifiers();
    }

    public boolean supportsParamsMetadata() {
        return flavor.supportsParamsMetadata();
    }

    public boolean supportsBatchUpdates() {
        return flavor.supportsBatchUpdates();
    }

    public boolean supportsCatalogs() {
        return flavor.supportsCatalogs();
    }

    public boolean supportsSchemas() {
        return flavor.supportsSchemas();
    }

    public DbTableMetadata getTable(String name) {
        TableFQName fqName = parseTableName(name);
        return tables.computeIfAbsent(fqName, this::loadTableMetadata);
    }

    public DbTableMetadata getTable(TableFQName tableName) {

        // the name can be full or partial (i.e. catalogs and schemas may or may not be there). Not trying to resolve
        // invariants on the assumption that this is how the caller would like to refer to this table throughout the
        // app... Worst case we'd get some duplicates

        return tables.computeIfAbsent(tableName, this::loadTableMetadata);
    }

    /**
     * Parses a table name String into a fully-qualified name object that provides information about table schema,
     * catalog and name. The incoming
     *
     * @param tableName a String identifying either fully-qualified or partial table name
     * @return a {@link TableFQName} object corresponding to the table.
     */
    // TODO: currently two related issues with this method
    //   1. will not work for DBs like Hana that are using dots as separators of their internal namespace that is
    //   otherwise the part of the table name.
    //   2. If table name components are passed in already quoted, this parser will not strip the quotes and hence
    //   will produce an incorrect FQN. (e.g. "c"."n")
    public TableFQName parseTableName(String tableName) {


        String[] parts = tableName.split("\\.", 3);
        switch (parts.length) {
            case 1:
                return new TableFQName(null, null, parts[0]);
            case 2:
                // the first part can be either a catalog or a schema
                if (supportsSchemas()) {
                    return new TableFQName(null, parts[0], parts[1]);
                } else if (supportsCatalogs()) {
                    return new TableFQName(parts[0], null, parts[1]);

                } else {
                    return new TableFQName(null, null, tableName);
                }
            case 3:
                if (supportsCatalogs() && supportsSchemas()) {
                    return new TableFQName(parts[0], parts[1], parts[2]);
                } else {
                    return new TableFQName(null, null, tableName);
                }

            default:
                return new TableFQName(null, null, tableName);
        }
    }

    private DbTableMetadata loadTableMetadata(TableFQName tableName) {

        Map<String, Integer> columnsAndTypes = new LinkedHashMap<>();
        Set<String> pks = new HashSet<>();
        Set<String> nullable = new HashSet<>();

        try (Connection c = dataSource.getConnection()) {

            DatabaseMetaData md = c.getMetaData();

            try (ResultSet columnsRs = md.getColumns(
                    tableName.catalog(), tableName.schema(), tableName.table(), null)) {

                while (columnsRs.next()) {
                    String name = columnsRs.getString("COLUMN_NAME");
                    int type = flavor.columnType(columnsRs.getInt("DATA_TYPE"), columnsRs.getString("TYPE_NAME"));
                    columnsAndTypes.put(name, type);

                    if ("YES".equals(columnsRs.getString("IS_NULLABLE"))) {
                        nullable.add(name);
                    }
                }
            }

            // sanity check ... table with no columns is possible, but suspect
            if (columnsAndTypes.isEmpty()) {

                try (ResultSet tablesRs = md.getTables(
                        tableName.catalog(), tableName.schema(), tableName.table(), null)) {
                    if (!tablesRs.next()) {
                        throw new RuntimeException("Non-existent table '" + tableName + "'");
                    }
                }
            }

            try (ResultSet pkRs = md.getPrimaryKeys(
                    tableName.catalog(), tableName.schema(), tableName.table())) {

                while (pkRs.next()) {
                    pks.add(pkRs.getString("COLUMN_NAME"));
                }

            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting info about table '" + tableName + "'", e);
        }

        int len = columnsAndTypes.size();
        DbColumnMetadata[] columns = new DbColumnMetadata[len];
        int i = 0;
        for (Map.Entry<String, Integer> e : columnsAndTypes.entrySet()) {
            String name = e.getKey();
            columns[i++] = new DbColumnMetadata(
                    name,
                    e.getValue(),
                    pks.contains(name),
                    nullable.contains(name));
        }

        return new DbTableMetadata(tableName, columns);
    }
}
