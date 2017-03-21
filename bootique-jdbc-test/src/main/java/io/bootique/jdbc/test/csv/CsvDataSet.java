package io.bootique.jdbc.test.csv;

import io.bootique.jdbc.test.Column;
import io.bootique.jdbc.test.InsertBuilder;
import io.bootique.jdbc.test.Table;
import io.bootique.resource.ResourceFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

/**
 * @since 0.14
 */
public class CsvDataSet {

    private Table table;
    private ResourceFactory csv;
    private ValueConverter converter;

    public CsvDataSet(Table table, ValueConverter converter, ResourceFactory csv) {
        this.table = table;
        this.csv = csv;
        this.converter = converter;
    }

    public void exec() {

        try (Reader csvReader = new InputStreamReader(csv.getUrl().openStream(), "UTF-8")) {
            try (CSVParser parser = new CSVParser(csvReader, CSVFormat.DEFAULT, 0, 0)) {

                Iterator<CSVRecord> it = parser.iterator();
                if(!it.hasNext()) {
                    return;
                }

                CSVRecord headerRow = it.next();
                String[] header = new String[headerRow.size()];
                for (int i = 0; i < header.length; i++) {
                    header[i] = headerRow.get(i);
                }

                InsertBuilder insertBuilder = table.insertColumns(header);

                it.forEachRemaining(row -> {

                    if (row.size() != header.length) {
                        throw new IllegalStateException("Row length "
                                + row.size()
                                + " is different from header length "
                                + header.length + ".");
                    }

                    Object[] values = new Object[header.length];
                    for (int i = 0; i < header.length; i++) {
                        Column column = insertBuilder.getColumns().get(i);
                        values[i] = converter.fromString(row.get(i), column);
                    }

                    insertBuilder.values(values);
                });

                insertBuilder.exec();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV " + csv, e);
        }
    }
}
