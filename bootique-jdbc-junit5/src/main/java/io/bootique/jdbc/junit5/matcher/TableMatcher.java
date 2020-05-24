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
import io.bootique.resource.ResourceFactory;

/**
 * Assists in making assertions about the data in a DB table.
 *
 * @since 2.0
 */
public class TableMatcher {

    private Table table;

    public TableMatcher(Table table) {
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    public RowCountMatcher eq(String column, Object value) {
        return new RowCountMatcher(table).eq(column, value);
    }

    /**
     * @since 1.1
     */
    public RowCountMatcher in(String column, Object... values) {
        return new RowCountMatcher(table).in(column, values);
    }

    public void assertMatches(int expectedRows) {
        new RowCountMatcher(table).assertMatches(expectedRows);
    }

    public void assertOneMatch() {
        new RowCountMatcher(table).assertOneMatch();
    }

    public void assertNoMatches() {
        new RowCountMatcher(table).assertNoMatches();
    }

    public void assertMatchesCsv(String csvResource, String... keyColumns) {
        assertMatchesCsv(new ResourceFactory(csvResource), keyColumns);
    }

    public void assertMatchesCsv(ResourceFactory csvResource, String... keyColumns) {
        new CsvMatcher(table).referenceCsvResource(csvResource).rowKeyColumns(keyColumns).assertMatches();
    }
}
