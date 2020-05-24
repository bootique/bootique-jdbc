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

package io.bootique.jdbc.junit5.connector;

import io.bootique.jdbc.junit5.metadata.DbColumnMetadata;
import io.bootique.jdbc.junit5.metadata.TableFQName;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.0
 */
public class InsertBuilder {

    protected TableFQName tableName;
    protected ExecStatementBuilder builder;
    protected DbColumnMetadata[] columns;
    protected List<Object[]> values;

    public InsertBuilder(ExecStatementBuilder builder, TableFQName tableName, DbColumnMetadata[] columns) {
        this.values = new ArrayList<>();
        this.tableName = tableName;
        this.columns = columns;
        this.builder = builder;
    }

    public InsertBuilder values(Object... values) {
        if (columns.length != values.length) {
            throw new IllegalArgumentException(tableName + ": values do not match columns. There are " + columns.length
                    + " column(s) " + "and " + values.length + " value(s).");
        }

        this.values.add(values);
        return this;
    }

    public DbColumnMetadata[] getColumns() {
        return columns;
    }

    public void exec() {

        builder.append("insert into ")
                .appendTableName(tableName)
                .append(" (");

        for (int i = 0; i < columns.length; i++) {

            DbColumnMetadata col = columns[i];

            if (i > 0) {
                builder.append(", ");
            }

            builder.appendIdentifier(col.getName());
        }

        builder.append(") values ");

        for (int i = 0; i < values.size(); i++) {

            if (i > 0) {
                builder.append(", ");
            }

            builder.append("(");

            Object[] rowValues = values.get(i);

            for (int j = 0; j < columns.length; j++) {
                if (j > 0) {
                    builder.append(", ");
                }

                builder.appendBinding(columns[j], rowValues[j]);
            }

            builder.append(")");
        }

        builder.exec();
    }
}
