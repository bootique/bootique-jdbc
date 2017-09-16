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

    public CsvStringsDataSetBuilder columns(String csvString) {
        return new CsvStringsDataSetBuilder(table, csvString, valueConverter);
    }

    public CsvStringsDataSetBuilder values(String csvString) {
        return new CsvStringsDataSetBuilder(table, valueConverter).values(csvString);
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
