package io.bootique.jdbc.test;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * JDBC utility class for setting up and analyzing the DB data sets for a single table.
 * Table intentionally bypasses Cayenne stack.
 */
public class Table {

    protected String name;
    protected JdbcStore store;
    protected List<Column> columns;

    public Table(JdbcStore store, String name) {
        this.store = store;
        this.name = name;
        this.columns = Collections.emptyList();
    }

    public Table(JdbcStore store, String name, String... columns) {
        this(store, name);
        setColumnNames(columns);
    }

    public UpdateWhereBuilder update() {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(store.quote(name)).append(" SET ");
        UpdatingSqlContext context = new UpdatingSqlContext(store, sql, new ArrayList<>());
        return new UpdateWhereBuilder(context);
    }

    public UpdateWhereBuilder delete() {

        StringBuilder sql = new StringBuilder();

        sql.append("DELETE FROM ").append(store.quote(name));
        UpdatingSqlContext context = new UpdatingSqlContext(store, sql, new ArrayList<>());

        return new UpdateWhereBuilder(context);
    }

    public int deleteAll() throws SQLException {
        return delete().execute();
    }

    public String getName() {
        return name;
    }

    public JdbcStore getStore() {
        return store;
    }

    /**
     * Sets columns that will be implicitly used in subsequent inserts and selects.
     */
    public Table setColumnNames(String... columnNames) {

        List<Column> columnList = new ArrayList<>(columnNames.length);
        for (String c : columnNames) {
            columnList.add(new Column(c));
        }

        this.columns = columnList;
        return this;
    }

    public Table setColumns(Column... columns) {
        this.columns = asList(columns);
        return this;
    }

    public Table insert(Object... values) {

        if (columns.size() != values.length) {
            throw new IllegalArgumentException("Values do not match columns. There are " + columns.size() + " column(s) " +
                    "and " + values.length + " value(s).");
        }

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(store.quote(name)).append(" (");

        List<Binding> bindings = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; i++) {

            Column col = columns.get(i);

            bindings.add(new Binding(col, values[i]));

            if (i > 0) {
                sql.append(", ");
            }

            sql.append(store.quote(col.getName()));
        }

        sql.append(") VALUES (");

        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sql.append(", ");
            }

            sql.append("?");
        }

        sql.append(")");

        store.execute(sql.toString(), bindings);
        return this;
    }

    /**
     * Selects a single row from the mapped table.
     */
    public Object[] selectOne() {

        if (columns.isEmpty()) {
            throw new IllegalArgumentException("No columns");
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(0);

            if (i > 0) {
                sql.append(", ");
            }
            sql.append(store.quote(col.getName()));
        }
        sql.append(" FROM ").append(store.quote(name));

        return new RowTemplate<Object[]>(store) {

            @Override
            Object[] readRow(ResultSet rs, String sql) throws SQLException {

                Object[] result = new Object[columns.size()];
                for (int i = 1; i <= result.length; i++) {
                    result[i - 1] = rs.getObject(i);
                }

                return result;
            }
        }.execute(sql.toString());
    }

    public List<Object[]> select() {

        if (columns.isEmpty()) {
            throw new IllegalArgumentException("No columns");
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(0);

            if (i > 0) {
                sql.append(", ");
            }
            sql.append(store.quote(col.getName()));
        }
        sql.append(" FROM ").append(store.quote(name));

        return new ResultSetTemplate<List<Object[]>>(store) {

            @Override
            protected List<Object[]> readResultSet(ResultSet rs, String sql) throws SQLException {
                List<Object[]> result = new ArrayList<Object[]>();
                while (rs.next()) {

                    Object[] row = new Object[columns.size()];
                    for (int i = 1; i <= row.length; i++) {
                        row[i - 1] = rs.getObject(i);
                    }

                    result.add(row);
                }

                return result;
            }
        }.execute(sql.toString());
    }

    public int getRowCount() {

        String sql = "SELECT COUNT(*) FROM " + store.quote(name);

        return new RowTemplate<Integer>(store) {

            @Override
            Integer readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getInt(1);
            }

        }.execute(sql);
    }

    public Object getObject(String column) {
        final String sql = "SELECT " + store.quote(column) + " FROM " + store.quote(name);

        return new RowTemplate<Object>(store) {

            @Override
            Object readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getObject(1);
            }

        }.execute(sql);
    }

    public byte getByte(String column) {
        final String sql = "SELECT " + store.quote(column) + " FROM " + store.quote(name);

        return new RowTemplate<Byte>(store) {

            @Override
            Byte readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getByte(1);
            }

        }.execute(sql);
    }

    public byte[] getBytes(String column) {
        final String sql = "SELECT " + store.quote(column) + " FROM " + store.quote(name);

        return new RowTemplate<byte[]>(store) {

            @Override
            byte[] readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getBytes(1);
            }

        }.execute(sql);
    }

    public int getInt(String column) {
        final String sql = "SELECT " + store.quote(column) + " FROM " + store.quote(name);

        return new RowTemplate<Integer>(store) {

            @Override
            Integer readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getInt(1);
            }

        }.execute(sql);
    }

    public long getLong(String column) {
        final String sql = "SELECT " + store.quote(column) + " FROM " + store.quote(name);

        return new RowTemplate<Long>(store) {

            @Override
            Long readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getLong(1);
            }

        }.execute(sql);
    }

    public double getDouble(String column) {
        final String sql = "SELECT " + store.quote(column) + " FROM " + store.quote(name);

        return new RowTemplate<Double>(store) {

            @Override
            Double readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getDouble(1);
            }

        }.execute(sql);
    }

    public boolean getBoolean(String column) {
        final String sql = "SELECT " + store.quote(column) + " FROM " + store.quote(name);

        return new RowTemplate<Boolean>(store) {

            @Override
            Boolean readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getBoolean(1);
            }

        }.execute(sql);
    }

    public String getString(String column) {

        final String sql = "SELECT " + store.quote(column) + " FROM " + store.quote(name);

        return new RowTemplate<String>(store) {

            @Override
            String readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getString(1);
            }

        }.execute(sql);
    }

    public java.util.Date getUtilDate(String column) {
        return getTimestamp(column);
    }

    public java.sql.Date getSqlDate(String column) {
        final String sql = "SELECT " + store.quote(column) + " FROM " + store.quote(name);

        return new RowTemplate<Date>(store) {

            @Override
            Date readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getDate(1);
            }

        }.execute(sql);
    }

    public Time getTime(String column) {
        final String sql = "SELECT " + store.quote(column) + " FROM " + store.quote(name);

        return new RowTemplate<Time>(store) {

            @Override
            Time readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getTime(1);
            }

        }.execute(sql);
    }

    public Timestamp getTimestamp(String column) {
        final String sql = "SELECT " + store.quote(column) + " FROM " + store.quote(name);

        return new RowTemplate<Timestamp>(store) {

            @Override
            Timestamp readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getTimestamp(1);
            }

        }.execute(sql);
    }
}
