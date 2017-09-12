package io.bootique.jdbc.test.matcher;

import io.bootique.jdbc.test.Column;
import io.bootique.jdbc.test.Table;
import io.bootique.jdbc.test.csv.CsvRecordSet;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @since 0.24
 */
class CsvMatcherRunner {

    private Table table;
    private CsvRecordSet referenceData;
    private RowKeyFactory keyFactory;

    public CsvMatcherRunner(Table table, CsvRecordSet referenceData, RowKeyFactory keyFactory) {
        this.table = table;
        this.referenceData = referenceData;
        this.keyFactory = keyFactory;
    }

    public void assertMatches() {

        if (referenceData.isEmpty()) {
            assertNoData();
            return;
        }

        matchData(mapTableData(readTableData()));
    }

    private void assertNoData() {
        table.matcher().assertNoMatch();
    }

    private void assertSizeMatches(CsvRecordSet referenceData, List<Object[]> tableData) {
        assertEquals("Reference data has " + referenceData.size() + " record(s). DB has " + tableData.size(),
                referenceData.size(),
                tableData.size());
    }

    private List<Object[]> readTableData() {
        List<Object[]> data = table.selectColumns(referenceData.getHeader());
        assertSizeMatches(referenceData, data);
        return data;
    }

    private void matchData(Map<RowKey, Object[]> mappedData) {
        referenceData.records().forEach(ref -> {

            RowKey rowKey = keyFactory.createKey(ref);
            Object[] row = mappedData.get(rowKey);
            assertNotNull("No DB records for key: " + rowKey, row);

            for (int i = 0; i < row.length; i++) {
                Object refVal = ref[i];
                Object dbVal = row[i];

                Column c = referenceData.getHeader().get(i);

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

    private Map<RowKey, Object[]> mapTableData(List<Object[]> data) {
        Map<RowKey, Object[]> mappedData = new HashMap<>();
        data.stream().forEach(row -> {

            RowKey key = keyFactory.createKey(row);

            // TODO: remove row key values from the rest of the row to speed up value comparision
            mappedData.put(key, row);
        });

        return mappedData;
    }

    private static void compareNull(Column c, RowKey rowKey, Object dbVal) {
        assertNull("Expected null value in column [" + c.getName() + "], row " + rowKey, dbVal);
    }

    private static void compareByteArrays(Column c, RowKey rowKey, byte[] refVal, byte[] dbVal) {
        assertArrayEquals("Unexpected value in column [" + c.getName() + "], row " + rowKey, refVal, dbVal);
    }

    private static void compareValues(Column c, RowKey rowKey, Object refVal, Object dbVal) {
        assertEquals("Unexpected value in column [" + c.getName() + "], row " + rowKey, refVal, dbVal);
    }

}
