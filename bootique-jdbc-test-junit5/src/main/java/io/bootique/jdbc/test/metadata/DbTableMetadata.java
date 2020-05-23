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
package io.bootique.jdbc.test.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 2.0
 */
public class DbTableMetadata {

    private TableFQName name;
    private DbColumnMetadata[] columns;

    private Map<String, DbColumnMetadata> columnsByName;
    private DbColumnMetadata[] pk;

    public DbTableMetadata(TableFQName name, DbColumnMetadata[] columns) {
        this.name = name;
        this.columns = columns;
        this.columnsByName = new HashMap<>();

        for (DbColumnMetadata column : columns) {

            DbColumnMetadata existing = columnsByName.put(column.getName(), column);
            if (existing != null && existing != column) {
                throw new IllegalArgumentException("Duplicate column name: " + column.getName());
            }
        }
    }

    public TableFQName getName() {
        return name;
    }

    public DbColumnMetadata[] getColumns() {
        return columns;
    }

    public String[] getColumnNames() {
        String[] names = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            names[i] = columns[i].getName();
        }
        return names;
    }

    public DbColumnMetadata[] getPkColumns() {

        if (pk == null) {
            this.pk = findPk();
        }

        return pk;
    }

    public DbColumnMetadata getColumn(String name) {

        DbColumnMetadata column = columnsByName.get(name);
        if (column == null) {
            throw new IllegalArgumentException("Column named '" + name + "' does not exist in table '" + this.name + "'");
        }

        return column;
    }

    private DbColumnMetadata[] findPk() {
        List<DbColumnMetadata> pk = new ArrayList<>();
        for (DbColumnMetadata c : columns) {
            if (c.isPk()) {
                pk.add(c);
            }
        }

        return pk.toArray(new DbColumnMetadata[0]);
    }
}
