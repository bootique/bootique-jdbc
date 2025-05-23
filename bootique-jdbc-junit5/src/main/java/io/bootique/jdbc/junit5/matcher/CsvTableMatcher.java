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
package io.bootique.jdbc.junit5.matcher;

import io.bootique.jdbc.junit5.Table;
import io.bootique.jdbc.junit5.dataset.CsvDataSetBuilder;
import io.bootique.jdbc.junit5.dataset.TableDataSet;
import io.bootique.jdbc.junit5.metadata.DbColumnMetadata;
import io.bootique.resource.ResourceFactory;
import org.apache.commons.csv.CSVFormat;

import java.util.Objects;

/**
 * Matches table data with a reference dataset coming from CSV data.
 *
 * @since 4.0
 */
public class CsvTableMatcher {

    private final Table table;
    private final CsvDataSetBuilder refDataSetBuilder;

    private String[] keyColumns;

    public CsvTableMatcher(Table table) {
        this.table = Objects.requireNonNull(table);
        this.refDataSetBuilder = table.csvDataSet();
    }

    public CsvTableMatcher keyColumns(String... keyColumns) {
        this.keyColumns = keyColumns;
        return this;
    }

    public CsvTableMatcher emptyStringIsNull() {
        return nullString("");
    }

    public CsvTableMatcher nullString(String nullString) {
        Objects.requireNonNull(nullString);
        refDataSetBuilder.format(CSVFormat.DEFAULT
                .builder()
                .setNullString(nullString)
                .build());

        return this;
    }

    public void assertMatches(String refCsv) {
        assertMatches(new ResourceFactory(refCsv));
    }

    public void assertMatches(ResourceFactory refCsv) {

        Objects.requireNonNull(refCsv);

        TableDataSet refDataSet = refDataSetBuilder.load(refCsv);
        RowKeyFactory keyFactory = createRowKeyFactory(refDataSet, keyColumns);
        new DataSetMatcher(table, refDataSet, keyFactory).assertMatches();
    }

    private RowKeyFactory createRowKeyFactory(TableDataSet refData, String... keyColumns) {
        if (keyColumns == null || keyColumns.length == 0) {
            DbColumnMetadata[] columns = refData.header();
            keyColumns = new String[columns.length];
            for (int i = 0; i < columns.length; i++) {
                keyColumns[i] = columns[i].getName();
            }
        }

        return RowKeyFactory.create(refData.header(), keyColumns);
    }
}
