package io.bootique.jdbc.test.dataset;

import io.bootique.jdbc.test.Table;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Objects;

/**
 * @since 0.24
 */
public class CsvStringsDataSetBuilder {

    private Table table;
    private boolean containsHeader;
    private FromStringConverter valueConverter;
    protected StringBuilder data;

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
        for(String row: csvStrings) {
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
            return new CsvReader(table, valueConverter).loadDataSet(table.getColumns(), reader);
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV", e);
        }
    }

    private Reader getDataReader() {
        return new StringReader(data.toString());
    }
}
