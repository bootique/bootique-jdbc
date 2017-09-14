package io.bootique.jdbc.test.csv;

import io.bootique.jdbc.test.Column;
import io.bootique.jdbc.test.Table;
import io.bootique.jdbc.test.dataset.TableDataSet;
import io.bootique.resource.ResourceFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @since 0.24
 */
public class CsvDataSetLoader {

    private Table table;
    private ResourceFactory csv;
    private ValueConverter converter;

    public CsvDataSetLoader(Table table, ValueConverter converter, ResourceFactory csv) {
        this.table = table;
        this.csv = csv;
        this.converter = converter;
    }

    public TableDataSet load() {

        try (Reader csvReader = new InputStreamReader(csv.getUrl().openStream(), "UTF-8")) {
            try (CSVParser parser = new CSVParser(csvReader, CSVFormat.DEFAULT, 0, 0)) {


                Iterator<CSVRecord> it = parser.iterator();
                if (!it.hasNext()) {
                    return new TableDataSet(table, Collections.emptyList(), Collections.emptyList());
                }

                CSVRecord headerRow = it.next();
                List<Column> header = new ArrayList<>(headerRow.size());
                for (String column : headerRow) {
                    header.add(table.getColumn(column));
                }

                if (!it.hasNext()) {
                    return new TableDataSet(table, header, Collections.emptyList());
                }

                List<Object[]> records = new ArrayList<>();
                while (it.hasNext()) {
                    records.add(getValues(it.next(), header));
                }

                return new TableDataSet(table, header, records);

            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV " + csv, e);
        }
    }

    Object[] getValues(CSVRecord record, List<Column> header) {

        Object[] values = new Object[record.size()];

        int len = header.size();
        for (int i = 0; i < len; i++) {
            values[i] = converter.fromString(record.get(i), header.get(i));
        }

        return values;
    }
}
