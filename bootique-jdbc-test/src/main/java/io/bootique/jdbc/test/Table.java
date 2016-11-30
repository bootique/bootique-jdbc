package io.bootique.jdbc.test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * JDBC utility class for setting up and analyzing the DB data sets for a single table.
 * Table intentionally bypasses Cayenne stack.
 */
public class Table {

    protected String name;
    protected DatabaseChannel channel;
    protected List<Column> columns;

    public static Builder builder(DatabaseChannel channel, String name) {
        return new Builder().channel(channel).name(name);
    }

    public UpdateWhereBuilder update() {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(channel.quote(name)).append(" SET ");
        UpdatingSqlContext context = new UpdatingSqlContext(channel, sql, new ArrayList<>());
        return new UpdateWhereBuilder(context);
    }

    public UpdateWhereBuilder delete() {

        StringBuilder sql = new StringBuilder();

        sql.append("DELETE FROM ").append(channel.quote(name));
        UpdatingSqlContext context = new UpdatingSqlContext(channel, sql, new ArrayList<>());

        return new UpdateWhereBuilder(context);
    }

    public int deleteAll() {
        return delete().execute();
    }

    public String getName() {
        return name;
    }

    public DatabaseChannel getChannel() {
        return channel;
    }

    /**
     * @return returns an immutable list of columns.
     * @since 0.13
     */
    public List<Column> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    /**
     * @param columns an array of columns that is a subset of the table columns.
     * @return a builder for insert query.
     * @since 0.13
     */
    public InsertBuilder insertColumns(String... columns) {

        if (columns == null) {
            throw new NullPointerException("Null columns");
        }

        if (columns.length == 0) {
            throw new IllegalArgumentException("No columns in the list");
        }

        List<String> includeNames = asList(columns);
        List<Column> subcolumns = this.columns.stream().filter(c -> includeNames.contains(c.getName()))
                .collect(Collectors.toList());

        return insertColumns(subcolumns);
    }

    public Table insert(Object... values) {
        insertColumns(columns).values(values).exec();
        return this;
    }

    protected InsertBuilder insertColumns(List<Column> columns) {
        if (columns == null) {
            throw new NullPointerException("Null columns");
        }

        if (columns.size() == 0) {
            throw new IllegalArgumentException("No columns in the list");
        }

        return new InsertBuilder(channel, name, columns);
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
            Column col = columns.get(i);

            if (i > 0) {
                sql.append(", ");
            }
            sql.append(channel.quote(col.getName()));
        }
        sql.append(" FROM ").append(channel.quote(name));

        return selectOne(sql.toString(), rs -> {
            Object[] result = new Object[columns.size()];

            try {
                for (int i = 1; i <= result.length; i++) {
                    result[i - 1] = rs.getObject(i);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }

            return result;
        });
    }

    protected <T> T selectOne(String sql, Function<ResultSet, T> rowReader) {

        List<T> result = channel.select(sql, 2, rowReader);
        switch (result.size()) {
            case 0:
                return null;
            case 1:
                return result.get(0);
            default:
                throw new IllegalArgumentException("At most one object expected in the result");
        }
    }

    private int columnIndex(String columnName) {

        for (int i = 0; i < columns.size(); i++) {

            if (columnName.equals(columns.get(i).getName())) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Performs select operation against the table, returning result as a map using provided unique column as a key.
     *
     * @param mapColumn the name of a unique column to use as a map key.
     * @return a map using provided unique column as a key.
     * @since 0.21
     */
    public Map<Object, Object[]> selectAsMap(String mapColumn) {

        int mapColumnIndex = columnIndex(mapColumn);
        if (mapColumnIndex < 0) {
            throw new IllegalArgumentException("Unknown column: " + mapColumn);
        }

        List<Object[]> list = select();

        Map<Object, Object[]> map = new HashMap<>();

        list.forEach(r -> {
            Object[] existing = map.put(r[mapColumnIndex], r);
            if (existing != null) {
                throw new IllegalArgumentException("More than one row matches '" + r[mapColumnIndex] + "' value");
            }
        });

        return map;
    }

    public List<Object[]> select() {

        if (columns.isEmpty()) {
            throw new IllegalArgumentException("No columns");
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(i);

            if (i > 0) {
                sql.append(", ");
            }
            sql.append(channel.quote(col.getName()));
        }
        sql.append(" FROM ").append(channel.quote(name));

        return channel.select(sql.toString(), Long.MAX_VALUE, rs -> {

            Object[] row = new Object[columns.size()];

            try {
                for (int i = 1; i <= row.length; i++) {
                    row[i - 1] = rs.getObject(i);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }

            return row;
        });
    }

    public int getRowCount() {

        String sql = "SELECT COUNT(*) FROM " + channel.quote(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getInt(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public Object getObject(String column) {
        String sql = "SELECT " + channel.quote(column) + " FROM " + channel.quote(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getObject(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public byte getByte(String column) {
        String sql = "SELECT " + channel.quote(column) + " FROM " + channel.quote(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getByte(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public byte[] getBytes(String column) {
        String sql = "SELECT " + channel.quote(column) + " FROM " + channel.quote(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getBytes(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public int getInt(String column) {
        String sql = "SELECT " + channel.quote(column) + " FROM " + channel.quote(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getInt(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public long getLong(String column) {
        String sql = "SELECT " + channel.quote(column) + " FROM " + channel.quote(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getLong(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public double getDouble(String column) {
        final String sql = "SELECT " + channel.quote(column) + " FROM " + channel.quote(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getDouble(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public boolean getBoolean(String column) {
        final String sql = "SELECT " + channel.quote(column) + " FROM " + channel.quote(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getBoolean(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public String getString(String column) {

        final String sql = "SELECT " + channel.quote(column) + " FROM " + channel.quote(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getString(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public java.util.Date getUtilDate(String column) {
        return getTimestamp(column);
    }

    public java.sql.Date getSqlDate(String column) {
        String sql = "SELECT " + channel.quote(column) + " FROM " + channel.quote(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getDate(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public Time getTime(String column) {
        String sql = "SELECT " + channel.quote(column) + " FROM " + channel.quote(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getTime(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public Timestamp getTimestamp(String column) {
        final String sql = "SELECT " + channel.quote(column) + " FROM " + channel.quote(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getTimestamp(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public static class Builder {

        private Table table;

        private Builder() {
            this.table = new Table();
        }

        public Table build() {
            return table;
        }

        public Builder name(String name) {
            table.name = name;
            return this;
        }

        public Builder channel(DatabaseChannel store) {
            table.channel = store;
            return this;
        }

        public Builder columnNames(String... columnNames) {

            List<Column> columns = new ArrayList<>(columnNames.length);
            for (String c : columnNames) {
                columns.add(new Column(c));
            }

            table.columns = columns;
            return this;
        }

        public Builder columns(Column... columns) {
            // must sort alphabetically for positional bindings
            List<Column> list = asList(columns);
            Collections.sort(list, Comparator.comparing(Column::getName));

            table.columns = list;
            return this;
        }
    }
}
