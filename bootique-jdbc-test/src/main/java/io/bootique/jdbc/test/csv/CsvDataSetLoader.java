package io.bootique.jdbc.test.csv;

import io.bootique.jdbc.test.Column;
import io.bootique.jdbc.test.Table;
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
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * @since 0.14
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

    public CsvDataSet read() {

        try (Reader csvReader = new InputStreamReader(csv.getUrl().openStream(), "UTF-8")) {
            try (CSVParser parser = new CSVParser(csvReader, CSVFormat.DEFAULT, 0, 0)) {


                Iterator<CSVRecord> it = parser.iterator();
                if (!it.hasNext()) {
                    return new CsvDataSet(table, converter, Collections.emptyList(), Collections.emptyList());
                }

                CSVRecord headerRow = it.next();
                List<Column> header = new ArrayList<>(headerRow.size());
                for (String column : headerRow) {
                    header.add(table.getColumn(column));
                }

                if (!it.hasNext()) {
                    return new CsvDataSet(table, converter, header, Collections.emptyList());
                }

                List<CSVRecord> records = new ArrayList<>();

                StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false)
                        .forEach(r -> {
                            if (r.size() != header.size()) {
                                throw new IllegalStateException("Row length "
                                        + r.size()
                                        + " is different from header length "
                                        + header.size() + ".");
                            }
                            records.add(r);
                        });

                return new CsvDataSet(table, converter, header, records);

            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV " + csv, e);
        }
    }
}
