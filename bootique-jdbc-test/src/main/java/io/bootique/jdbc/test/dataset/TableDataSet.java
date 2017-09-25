package io.bootique.jdbc.test.dataset;

import io.bootique.jdbc.test.Column;
import io.bootique.jdbc.test.InsertBuilder;
import io.bootique.jdbc.test.Table;

import java.util.List;
import java.util.Objects;

/**
 * Represents data from a single table.
 *
 * @since 0.24
 */
public class TableDataSet implements DataSet {

    private Table table;
    private List<Column> header;
    private List<Object[]> records;

    public TableDataSet(Table table, List<Column> header, List<Object[]> records) {
        this.header = Objects.requireNonNull(header);
        this.records = Objects.requireNonNull(records);
        this.table = Objects.requireNonNull(table);
    }

    public Table getTable() {
        return table;
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

    public List<Object[]> getRecords() {
        return records;
    }

    /**
     * Inserts data set records to the underlying DB table.
     */
    @Override
    public void persist() {
        if (size() > 0) {
            InsertBuilder builder = table.insertColumns(getHeader());
            records.forEach(row -> builder.values(row));
            builder.exec();
        }
    }
}
