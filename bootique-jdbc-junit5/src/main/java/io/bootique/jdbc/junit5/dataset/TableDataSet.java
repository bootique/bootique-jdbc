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

package io.bootique.jdbc.junit5.dataset;

import io.bootique.jdbc.junit5.connector.InsertBuilder;
import io.bootique.jdbc.junit5.Table;
import io.bootique.jdbc.junit5.metadata.DbColumnMetadata;

import java.util.List;
import java.util.Objects;

/**
 * Represents data from a single table.
 *
 * @since 2.0
 */
public class TableDataSet implements DataSet {

    private Table table;
    private DbColumnMetadata[] header;
    private List<Object[]> records;

    public TableDataSet(Table table, DbColumnMetadata[] header, List<Object[]> records) {
        this.header = Objects.requireNonNull(header);
        this.records = Objects.requireNonNull(records);
        this.table = Objects.requireNonNull(table);
    }

    public Table getTable() {
        return table;
    }

    public DbColumnMetadata[] getHeader() {
        return header;
    }

    public int size() {
        return records.size();
    }

    public boolean isEmpty() {
        return records.isEmpty();
    }

    public List<Object[]> getRecords() {
        return records;
    }

    /**
     * Inserts data set records to the underlying DB table.
     */
    @Override
    public void persist() {
        if (size() > 0) {
            InsertBuilder builder = table.insertColumns(getHeader());
            records.forEach(builder::values);
            builder.exec();
        }
    }
}
