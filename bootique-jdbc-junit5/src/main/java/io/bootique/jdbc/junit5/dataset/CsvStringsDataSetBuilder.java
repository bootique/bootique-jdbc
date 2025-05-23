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

import io.bootique.jdbc.junit5.Table;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Objects;

/**
 * A builder that assembles a data set matching a {@link Table} structure from in-memory CSV-like strings.
 */
public class CsvStringsDataSetBuilder {

    protected StringBuilder data;
    private final Table table;
    private boolean containsHeader;
    private FromStringConverter valueConverter;

    public CsvStringsDataSetBuilder(Table table, FromStringConverter valueConverter) {
        this(table, null, valueConverter);
    }

    public CsvStringsDataSetBuilder(Table table, String header, FromStringConverter valueConverter) {
        this.table = Objects.requireNonNull(table);
        this.valueConverter = Objects.requireNonNull(valueConverter);
        this.data = new StringBuilder();

        if (header != null) {
            data.append(header).append("\n");
            containsHeader = true;
        }
    }

    public CsvStringsDataSetBuilder valueConverter(FromStringConverter converter) {
        this.valueConverter = Objects.requireNonNull(converter);
        return this;
    }

    public CsvStringsDataSetBuilder rows(String... csvStrings) {
        for (String row : csvStrings) {
            data.append(row).append("\n");
        }
        return this;
    }

    public TableDataSet build() {
        return containsHeader ? buildWithStringHeader() : buildWithTableHeader();
    }

    public void persist() {
        build().persist();
    }

    private TableDataSet buildWithStringHeader() {
        try (Reader reader = getDataReader()) {
            return new CsvReader(table, valueConverter).loadDataSet(reader);
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV", e);
        }
    }

    private TableDataSet buildWithTableHeader() {
        try (Reader reader = getDataReader()) {
            return new CsvReader(table, valueConverter).loadDataSet(table.getMetadata().getColumns(), reader);
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV", e);
        }
    }

    private Reader getDataReader() {
        return new StringReader(data.toString());
    }
}
