package io.bootique.jdbc.test.csv;

import io.bootique.jdbc.test.Column;
import org.apache.commons.csv.CSVRecord;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class CsvRecordSet {

    private ValueConverter converter;
    private List<Column> header;
    private List<CSVRecord> records;

    public CsvRecordSet(ValueConverter converter, List<Column> header, List<CSVRecord> records) {
        this.header = Objects.requireNonNull(header);
        this.records = Objects.requireNonNull(records);
        this.converter = Objects.requireNonNull(converter);
    }

    public List<Column> getHeader() {
        return header;
    }

    public int size() {
        return records.size();
    }

    public boolean isEmpty() {
        return records.isEmpty();
    }

    public Stream<Object[]> records() {
        return records.stream().map(this::getValues);
    }

    Object[] getValues(CSVRecord record) {

        Object[] values = new Object[record.size()];

        int len = header.size();
        for (int i = 0; i < len; i++) {
            values[i] = converter.fromString(record.get(i), header.get(i));
        }

        return values;
    }
}
