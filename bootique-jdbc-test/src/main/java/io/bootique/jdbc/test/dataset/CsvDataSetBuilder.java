/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.jdbc.test.dataset;

import io.bootique.jdbc.test.Table;
import io.bootique.resource.ResourceFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;

/**
 * @since 0.24
 */
public class CsvDataSetBuilder {

    private Table table;
    private FromStringConverter valueConverter;

    public CsvDataSetBuilder(Table table) {
        this.table = table;
        this.valueConverter = DefaultFromStringConverter.DEFAULT_CONVERTER;
    }

    public CsvDataSetBuilder valueConverter(FromStringConverter converter) {
        this.valueConverter = Objects.requireNonNull(converter);
        return this;
    }

    /**
     * Starts building a data set with the specified columns.
     *
     * @param csvString CSV string specifying DataSet columns.
     * @return a builder for API-based DataSet.
     */
    public CsvStringsDataSetBuilder columns(String csvString) {
        return new CsvStringsDataSetBuilder(table, csvString, valueConverter);
    }

    /**
     * Starts building a data set with columns matching the underlying table columns.
     *
     * @param csvStrings an array of String, each String representing a CSV row in the DataSet.
     * @return a builder for API-based DataSet.
     */
    public CsvStringsDataSetBuilder rows(String... csvStrings) {
        return new CsvStringsDataSetBuilder(table, valueConverter).rows(csvStrings);
    }

    public TableDataSet load(String csvResource) {
        return load(new ResourceFactory(csvResource));
    }

    public TableDataSet load(ResourceFactory csvResource) {

        try (Reader reader = new InputStreamReader(csvResource.getUrl().openStream(), "UTF-8")) {
            return new CsvReader(table, valueConverter).loadDataSet(reader);
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV " + csvResource, e);
        }
    }
}
