package io.bootique.jdbc.test.dataset;

import io.bootique.jdbc.test.Column;
import io.bootique.jdbc.test.Table;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class CsvReader {

    private FromStringConverter valueConverter;
    private Table table;

    CsvReader(Table table, FromStringConverter valueConverter) {
        this.valueConverter = valueConverter;
        this.table = table;
    }

    TableDataSet loadDataSet(Reader dataReader) throws IOException {
        try (CSVParser parser = new CSVParser(dataReader, CSVFormat.DEFAULT, 0, 0)) {

            Iterator<CSVRecord> rows = parser.iterator();
            if (!rows.hasNext()) {
                return new TableDataSet(table, Collections.emptyList(), Collections.emptyList());
            }

            List<Column> header = getHeader(rows.next());
            return readData(header, rows);
        }
    }

    TableDataSet loadDataSet(List<Column> header, Reader dataReader) throws IOException {
        try (CSVParser parser = new CSVParser(dataReader, CSVFormat.DEFAULT, 0, 0)) {
            return readData(header, parser.iterator());
        }
    }

    private List<Column> getHeader(CSVRecord record) {
        List<Column> header = new ArrayList<>(record.size());
        for (String column : record) {
            header.add(table.getColumn(column));
        }
        return header;
    }

    private Object[] getRow(CSVRecord csvRow, List<Column> header) {

        Object[] row = new Object[csvRow.size()];

        int len = header.size();
        for (int i = 0; i < len; i++) {
            row[i] = valueConverter.fromString(csvRow.get(i), header.get(i));
        }

        return row;
    }

    private TableDataSet readData(List<Column> header, Iterator<CSVRecord> rows) throws IOException {

        List<Object[]> records = new ArrayList<>();
        while (rows.hasNext()) {
            records.add(getRow(rows.next(), header));
        }

        return new TableDataSet(table, header, records);
    }
}
