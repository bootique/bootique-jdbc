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
