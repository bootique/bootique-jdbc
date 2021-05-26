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

package io.bootique.jdbc.test;

import io.bootique.jdbc.test.jdbc.ExecStatementBuilder;

import java.util.ArrayList;
import java.util.List;

public class InsertBuilder {

    protected String tableName;
    protected ExecStatementBuilder builder;
    protected List<Column> columns;
    protected List<Object[]> values;

    public InsertBuilder(ExecStatementBuilder builder, String tableName, List<Column> columns) {
        this.values = new ArrayList<>();
        this.tableName = tableName;
        this.columns = columns;
        this.builder = builder;
    }

    public InsertBuilder values(Object... values) {
        if (columns.size() != values.length) {
            throw new IllegalArgumentException(tableName + ": values do not match columns. There are " + columns.size()
                    + " column(s) " + "and " + values.length + " value(s).");
        }

        this.values.add(values);
        return this;
    }

    /**
     * Returns a list of columns for this insert.
     *
     * @return a list of columns for this insert.
     */
    public List<Column> getColumns() {
        return columns;
    }

    public void exec() {

        builder.append("INSERT INTO ")
                .appendIdentifier(tableName)
                .append(" (");

        for (int i = 0; i < columns.size(); i++) {

            Column col = columns.get(i);

            if (i > 0) {
                builder.append(", ");
            }

            builder.appendIdentifier(col.getName());
        }

        builder.append(") VALUES ");

        for (int i = 0; i < values.size(); i++) {

            if (i > 0) {
                builder.append(", ");
            }

            builder.append("(");

            Object[] rowValues = values.get(i);

            for (int j = 0; j < columns.size(); j++) {
                if (j > 0) {
                    builder.append(", ");
                }

                builder.appendBinding(columns.get(j), rowValues[j]);
            }

            builder.append(")");
        }

        builder.exec();
    }
}
