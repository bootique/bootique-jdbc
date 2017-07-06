package io.bootique.jdbc.test;

import io.bootique.jdbc.test.csv.CsvDataSet;
import io.bootique.jdbc.test.csv.ValueConverter;
import io.bootique.resource.ResourceFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
import java.util.Objects;
import java.util.function.Function;

import static java.util.Arrays.asList;

/**
 * JDBC utility class for setting up and analyzing the DB data sets for a single table.
 * Table intentionally bypasses Cayenne stack.
 */
public class Table {

    protected String name;
    protected DatabaseChannel channel;
    protected List<Column> columns;
    protected IdentifierQuotationStrategy quotationStrategy;

    public static Builder builder(DatabaseChannel channel, String name) {
        return new Builder().channel(channel).name(name);
    }

    public UpdateWhereBuilder update() {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(quotationStrategy.quoted(name)).append(" SET ");
        UpdatingSqlContext context = new UpdatingSqlContext(channel, quotationStrategy, sql, new ArrayList<>());
        return new UpdateWhereBuilder(context);
    }

    /**
     * Update table statement
     *
     * @return {@link UpdateSetBuilder}
     * @since 0.23
     */
    public UpdateSetBuilder updateSet() {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(quotationStrategy.quoted(name)).append(" SET ");
        UpdatingSqlContext context = new UpdatingSqlContext(channel, quotationStrategy, sql, new ArrayList<>());
        return new UpdateSetBuilder(context);
    }

    public UpdateWhereBuilder delete() {

        StringBuilder sql = new StringBuilder();

        sql.append("DELETE FROM ").append(quotationStrategy.quoted(name));
        UpdatingSqlContext context = new UpdatingSqlContext(channel, quotationStrategy, sql, new ArrayList<>());

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
     * @return an internal IdentifierQuotationStrategy used to generate quoted SQL identifiers.
     * @since 0.14
     */
    public IdentifierQuotationStrategy getQuotationStrategy() {
        return quotationStrategy;
    }

    /**
     * @return returns an immutable list of columns.
     * @since 0.13
     */
    public List<Column> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    /**
     * @param name column name
     * @return a column for name.
     * @since 0.14
     */
    public Column getColumn(String name) {

        for(Column c : columns) {
            if(name.equals(c.getName())) {
                return c;
            }
        }

        throw new IllegalArgumentException("No such column: " + name);
    }

    /**
     * @param columns an array of columns that is a subset of the table columns.
     * @return a builder for insert query.
     * @since 0.13
     */
    public InsertBuilder insertColumns(String... columns) {
        return insertColumns(toColumnsList(columns));
    }

    /**
     * Inserts test data from the provided CSV resource.
     *
     * @param csvResource a resource that stores CSV data.
     * @return this table instance.
     * @since 0.14
     */
    public Table insertFromCsv(String csvResource) {
        return insertFromCsv(new ResourceFactory(csvResource));
    }

    /**
     * Inserts test data from the provided CSV resource.
     *
     * @param csvResource a resource that stores CSV data.
     * @return this table instance.
     * @since 0.14
     */
    public Table insertFromCsv(ResourceFactory csvResource) {
        new CsvDataSet(this, new ValueConverter(), csvResource).insert();
        return this;
    }

    public Table insert(Object... values) {
        insertColumns(columns).values(values).exec();
        return this;
    }

    public InsertBuilder insertColumns(List<Column> columns) {
        if (columns == null) {
            throw new NullPointerException("Null columns");
        }

        if (columns.size() == 0) {
            throw new IllegalArgumentException("No columns in the list");
        }

        return new InsertBuilder(channel, quotationStrategy, name, columns);
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
            sql.append(quotationStrategy.quoted(col.getName()));
        }
        sql.append(" FROM ").append(quotationStrategy.quoted(name));

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

    /**
     * @param columns an array of columns to select/
     * @return select result.
     * @since 0.14
     */
    public List<Object[]> selectColumns(String... columns) {
        return selectColumns(toColumnsList(columns));
    }

    public List<Object[]> select() {
        return selectColumns(this.columns);
    }

    public List<Object[]> selectColumns(List<Column> columns) {

        if (columns.isEmpty()) {
            throw new IllegalArgumentException("No columns");
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(i);

            if (i > 0) {
                sql.append(", ");
            }
            sql.append(quotationStrategy.quoted(col.getName()));
        }
        sql.append(" FROM ").append(quotationStrategy.quoted(name));

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

        String sql = "SELECT COUNT(*) FROM " + quotationStrategy.quoted(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getInt(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public Object getObject(String column) {
        String sql = "SELECT " + quotationStrategy.quoted(column) + " FROM " + quotationStrategy.quoted(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getObject(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public byte getByte(String column) {
        String sql = "SELECT " + quotationStrategy.quoted(column) + " FROM " + quotationStrategy.quoted(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getByte(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public byte[] getBytes(String column) {
        String sql = "SELECT " + quotationStrategy.quoted(column) + " FROM " + quotationStrategy.quoted(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getBytes(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public int getInt(String column) {
        String sql = "SELECT " + quotationStrategy.quoted(column) + " FROM " + quotationStrategy.quoted(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getInt(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public long getLong(String column) {
        String sql = "SELECT " + quotationStrategy.quoted(column) + " FROM " + quotationStrategy.quoted(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getLong(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public double getDouble(String column) {
        final String sql = "SELECT " + quotationStrategy.quoted(column) + " FROM " + quotationStrategy.quoted(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getDouble(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public boolean getBoolean(String column) {
        final String sql = "SELECT " + quotationStrategy.quoted(column) + " FROM " + quotationStrategy.quoted(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getBoolean(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public String getString(String column) {

        final String sql = "SELECT " + quotationStrategy.quoted(column) + " FROM " + quotationStrategy.quoted(name);

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
        String sql = "SELECT " + quotationStrategy.quoted(column) + " FROM " + quotationStrategy.quoted(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getDate(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public Time getTime(String column) {
        String sql = "SELECT " + quotationStrategy.quoted(column) + " FROM " + quotationStrategy.quoted(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getTime(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    public Timestamp getTimestamp(String column) {
        final String sql = "SELECT " + quotationStrategy.quoted(column) + " FROM " + quotationStrategy.quoted(name);

        return selectOne(sql, rs -> {
            try {
                return rs.getTimestamp(1);
            } catch (SQLException e) {
                throw new RuntimeException("Error reading ResultSet", e);
            }
        });
    }

    /**
     * @param csvResource a CSV resource to use in data comparisons.
     * @param rowKey      An array of columns that uniquely identify a row. Each column must be present in CSV. By default
     *                    all row columns are used in comparision.
     * @since 0.14
     */
    public void contentsMatchCsv(String csvResource, String... rowKey) {
        contentsMatchCsv(new ResourceFactory(csvResource), rowKey);
    }

    /**
     * @param csvResource a CSV resource to use in data comparisons.
     * @param rowKey      An array of columns that uniquely identify a row. Each column must be present in CSV. By default
     *                    all row columns are used in comparision.
     * @since 0.14
     */
    public void contentsMatchCsv(ResourceFactory csvResource, String... rowKey) {
        new CsvDataSet(this, new ValueConverter(), csvResource).matchContents(rowKey);
    }


    protected List<Column> toColumnsList(String... columns) {
        if (columns == null) {
            throw new NullPointerException("Null columns");
        }

        if (columns.length == 0) {
            throw new IllegalArgumentException("No columns in the list");
        }

        Map<String, Column> allColumnsMap = new HashMap<>();
        this.columns.forEach(c -> allColumnsMap.put(c.getName(), c));

        List<Column> subcolumns = new ArrayList<>();
        for (String name : columns) {
            Column c = allColumnsMap.computeIfAbsent(name, key -> {
                throw new IllegalArgumentException("'" + key + "' is not a valid column");
            });
            subcolumns.add(c);
        }

        return subcolumns;
    }

    public static class Builder {

        private Table table;
        private boolean quotingSqlIdentifiers;
        private boolean initColumnTypesFromDBMetadata;

        private Builder() {
            this.table = new Table();
            this.quotingSqlIdentifiers = true;
        }

        public Table build() {

            Objects.requireNonNull(table.channel);
            String quoteSymbol = table.channel.getIdentifierQuote();

            table.quotationStrategy = quotingSqlIdentifiers
                    ? id -> quoteSymbol + Objects.requireNonNull(id) + quoteSymbol
                    : id -> id;

            if (initColumnTypesFromDBMetadata) {
                doInitColumnTypesFromDBMetadata();
            }

            return table;
        }

        private void doInitColumnTypesFromDBMetadata() {

            if (table.columns.isEmpty()) {
                return;
            }

            List<Column> updatedColumns = new ArrayList<>(table.columns.size());

            try (Connection c = table.channel.getConnection()) {

                DatabaseMetaData md = c.getMetaData();
                Map<String, Integer> types = new HashMap<>();
                try (ResultSet rs = md.getColumns(null, null, table.getName(), "%")) {
                    while (rs.next()) {
                        types.put(rs.getString("COLUMN_NAME"), rs.getInt("DATA_TYPE"));
                    }
                }

                table.columns.stream()
                        .map(col -> new Column(col.getName(), types.get(col.getName())))
                        .forEach(updatedColumns::add);

            } catch (SQLException e) {
                throw new RuntimeException("Error getting DB metadata", e);
            }

            table.columns = updatedColumns;
        }

        public Builder name(String name) {
            table.name = name;
            return this;
        }

        /**
         * Sets whether SQL identifiers should be quoted in the generated SQL using DB-specific quotation symbol.
         * True by default.
         *
         * @param shouldQuote a flag indicating whether SQL identifiers should be surrounded in quotations.
         * @return this builder instance.
         * @since 0.14
         */
        public Builder quoteSqlIdentifiers(boolean shouldQuote) {
            this.quotingSqlIdentifiers = shouldQuote;
            return this;
        }

        public Builder channel(DatabaseChannel channel) {
            table.channel = channel;
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

        public Builder initColumnTypesFromDBMetadata() {
            this.initColumnTypesFromDBMetadata = true;
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
