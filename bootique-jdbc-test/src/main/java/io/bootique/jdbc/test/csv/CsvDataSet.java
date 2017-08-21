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
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static org.junit.Assert.*;

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

    public void matchContents(String... rowKeyColumns) {

        CsvRecordSet refSet = read();
        if (refSet.size() == 0) {
            assertEquals("Expected empty table", 0, table.getRowCount());
        }

        if (rowKeyColumns == null || rowKeyColumns.length == 0) {
            rowKeyColumns = refSet.getHeader().stream().map(Column::getName).toArray(i -> new String[i]);
        }

        RowKeyFactory keyFactory = RowKeyFactory.create(refSet.getHeader(), rowKeyColumns);

        List<Object[]> data = table.selectColumns(refSet.getHeader());
        assertEquals("Reference dataset has " + refSet.size() + " record(s). DB has " + data.size(),
                refSet.size(),
                data.size());

        Map<RowKey, Object[]> mappedData = new HashMap<>();
        data.stream().forEach(row -> {

            RowKey key = keyFactory.createKey(row);

            // TODO: remove row key values from the rest of the row to speed up value comparision
            mappedData.put(key, row);
        });

        refSet.records().forEach(ref -> {

            RowKey rowKey = keyFactory.createKey(ref);
            Object[] row = mappedData.get(rowKey);
            assertNotNull("No DB records for key: " + rowKey, row);

            for (int i = 0; i < row.length; i++) {
                Object refVal = ref[i];
                Object dbVal = row[i];

                Column c = refSet.getHeader().get(i);

                if (refVal == null) {
                    compareNull(c, rowKey, dbVal);
                    continue;
                }

                switch (c.getType()) {
                    case Types.VARBINARY:
                    case Types.BINARY:
                    case Types.LONGVARBINARY:
                    case Types.BLOB:
                        // TODO: check that data type is actually a byte[]?
                        compareByteArrays(c, rowKey, (byte[]) refVal, (byte[]) dbVal);
                        break;
                    default:
                        compareValues(c, rowKey, refVal, dbVal);
                        break;
                }
            }
        });
    }

    private void compareNull(Column c, RowKey rowKey, Object dbVal) {
        assertNull("Expected null value in column [" + c.getName() + "], row " + rowKey, dbVal);
    }

    private void compareByteArrays(Column c, RowKey rowKey, byte[] refVal, byte[] dbVal) {
        assertArrayEquals("Unexpected value in column [" + c.getName() + "], row " + rowKey, refVal, dbVal);
    }

    private void compareValues(Column c, RowKey rowKey, Object refVal, Object dbVal) {
        assertEquals("Unexpected value in column [" + c.getName() + "], row " + rowKey, refVal, dbVal);
    }


    public void insert() {

        CsvRecordSet recordSet = read();
        if (recordSet.size() != 0) {
            InsertBuilder builder = table.insertColumns(recordSet.getHeader());
            recordSet.records().forEach(row -> builder.values(row));
            builder.exec();
        }
    }

    protected CsvRecordSet read() {

        try (Reader csvReader = new InputStreamReader(csv.getUrl().openStream(), "UTF-8")) {
            try (CSVParser parser = new CSVParser(csvReader, CSVFormat.DEFAULT, 0, 0)) {


                Iterator<CSVRecord> it = parser.iterator();
                if (!it.hasNext()) {
                    return new CsvRecordSet(converter, Collections.emptyList(), Collections.emptyList());
                }

                CSVRecord headerRow = it.next();
                List<Column> header = new ArrayList<>(headerRow.size());
                for (String column : headerRow) {
                    header.add(table.getColumn(column));
                }

                if (!it.hasNext()) {
                    return new CsvRecordSet(converter, header, Collections.emptyList());
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

                return new CsvRecordSet(converter, header, records);

            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV " + csv, e);
        }
    }
}
