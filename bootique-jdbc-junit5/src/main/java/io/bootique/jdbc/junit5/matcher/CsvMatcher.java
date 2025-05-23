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
import io.bootique.jdbc.junit5.dataset.TableDataSet;
import io.bootique.jdbc.junit5.metadata.DbColumnMetadata;
import io.bootique.resource.ResourceFactory;

import java.util.Objects;

/**
 * @since 2.0
 * @deprecated in favor of {@link CsvTableMatcher} and {@link Table#csvMatcher()}
 */
@Deprecated(since = "4.0", forRemoval = true)
public class CsvMatcher {

    private final Table table;
    private ResourceFactory referenceCsvResource;
    private String[] keyColumns;

    public CsvMatcher(Table table) {
        this.table = Objects.requireNonNull(table);
    }

    public CsvMatcher referenceCsvResource(ResourceFactory csvResource) {
        this.referenceCsvResource = csvResource;
        return this;
    }

    public CsvMatcher rowKeyColumns(String... keyColumns) {
        this.keyColumns = keyColumns;
        return this;
    }

    public void assertMatches() {

        Objects.requireNonNull(referenceCsvResource);

        TableDataSet refDataSet = table.csvDataSet().load(referenceCsvResource);
        RowKeyFactory keyFactory = createRowKeyFactory(refDataSet, keyColumns);
        new DataSetMatcher(table, refDataSet, keyFactory).assertMatches();
    }

    private RowKeyFactory createRowKeyFactory(TableDataSet refData, String... keyColumns) {
        if (keyColumns == null || keyColumns.length == 0) {
            DbColumnMetadata[] columns = refData.header();
            keyColumns = new String[columns.length];
            for(int i = 0; i < columns.length; i++) {
                keyColumns[i] = columns[i].getName();
            }
        }

        return RowKeyFactory.create(refData.header(), keyColumns);
    }
}
