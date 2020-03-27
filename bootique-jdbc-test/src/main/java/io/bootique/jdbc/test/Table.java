/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.jdbc.test;

import io.bootique.jdbc.test.dataset.CsvDataSetBuilder;
import io.bootique.jdbc.test.jdbc.ExecStatementBuilder;
import io.bootique.jdbc.test.jdbc.RowReader;
import io.bootique.jdbc.test.jdbc.SelectStatementBuilder;
import io.bootique.jdbc.test.matcher.TableMatcher;

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
     * @return a new {@link ExecStatementBuilder} object that assists in creating and executing a PreparedStatement
     * using policies specified for this table.
     * @since 0.24
     */
    public ExecStatementBuilder execStatement() {
        return getChannel().execStatement().quoteIdentifiersWith(quotationStrategy);
    }

    /**
     * @param rowReader a function that converts a ResultSet row into an object.
     * @param <T>       the type of objects read by returned statement builder.
     * @return a new {@link SelectStatementBuilder} object that assists in creating and running a selecting
     * PreparedStatement using policies specified for this table.
     * @since 0.24
     */
    public <T> SelectStatementBuilder<T> selectStatement(RowReader<T> rowReader) {
        return getChannel().selectStatement(rowReader).quoteIdentifiersWith(quotationStrategy);
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

        for (Column c : columns) {
            if (name.equals(c.getName())) {
                return c;
            }
        }

        throw new IllegalArgumentException("No such column: " + name);
    }

    /**
     * Update table statement
     *
     * @return {@link UpdateSetBuilder}
     * @since 0.15
     */
    public UpdateSetBuilder update() {
        ExecStatementBuilder builder = execStatement()
                .append("UPDATE ")
                .appendIdentifier(name)
                .append(" SET ");

        return new UpdateSetBuilder(builder);
    }

    public UpdateWhereBuilder delete() {
        ExecStatementBuilder builder = execStatement()
                .append("DELETE FROM ")
                .appendIdentifier(name);

        return new UpdateWhereBuilder(builder);
    }

    public int deleteAll() {
        return delete().exec();
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
     * Returns a builder object to assemble a data set matching this table structure either from CSV-like strings,
     * or from a CSV file resource.
     *
     * @return a builder of a {@link io.bootique.jdbc.test.dataset.TableDataSet}.
     * @since 0.24
     */
    public CsvDataSetBuilder csvDataSet() {
        return new CsvDataSetBuilder(this);
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

        return new InsertBuilder(execStatement(), name, columns);
    }

    /**
     * @return a new instance of {@link TableMatcher} for this table that allows to make assertions about the table data.
     * @since 0.24
     */
    public TableMatcher matcher() {
        return new TableMatcher(this);
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
     * Selects all data from the table.
     *
     * @return a List of Object[] where each array represents a row in the underlying table.
     */
    public List<Object[]> select() {
        return selectColumns(this.columns);
    }

    /**
     * Selects a single row from the mapped table.
     */
    public Object[] selectOne() {
        return ensureAtMostOneRow(selectColumnsBuilder(this.columns), null);
    }

    /**
     * @param columns an array of columns to select.
     * @return a List of Object[] where each array represents a row in the underlying table made of columns requested
     * in this method.
     * @since 0.14
     */
    public List<Object[]> selectColumns(String... columns) {
        return selectColumns(toColumnsList(columns));
    }

    public List<Object[]> selectColumns(List<Column> columns) {
        return selectColumnsBuilder(columns).select(Long.MAX_VALUE);
    }

    protected SelectStatementBuilder<Object[]> selectColumnsBuilder(List<Column> columns) {
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("No columns");
        }

        SelectStatementBuilder<Object[]> builder = this
                .selectStatement(RowReader.arrayReader(columns.size()))
                .append("SELECT ");

        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(i);

            if (i > 0) {
                builder.append(", ");
            }
            builder.appendIdentifier(col.getName());
        }

        return builder.append(" FROM ").appendIdentifier(name);
    }

    protected <T> T selectColumn(String columnName, RowReader<T> reader) {
        return selectColumn(columnName, reader, null);
    }

    protected <T> T selectColumn(String columnName, RowReader<T> reader, T defaultValue) {
        SelectStatementBuilder<T> builder = selectStatement(reader)
                .append("SELECT ")
                .appendIdentifier(columnName)
                .append(" FROM ")
                .appendIdentifier(name);
        return ensureAtMostOneRow(builder, defaultValue);
    }

    public Object getObject(String column) {
        return selectColumn(column, RowReader.objectReader());
    }

    public byte getByte(String column) {
        return selectColumn(column, RowReader.byteReader(), (byte) 0);
    }

    public byte[] getBytes(String column) {
        return selectColumn(column, RowReader.bytesReader());
    }

    public int getInt(String column) {
        return selectColumn(column, RowReader.intReader(), 0);
    }

    public long getLong(String column) {
        return selectColumn(column, RowReader.longReader(), 0L);
    }

    public double getDouble(String column) {
        return selectColumn(column, RowReader.doubleReader(), 0.0);
    }

    public boolean getBoolean(String column) {
        return selectColumn(column, RowReader.booleanReader(), false);
    }

    public String getString(String column) {
        return selectColumn(column, RowReader.stringReader());
    }

    public java.util.Date getUtilDate(String column) {
        return getTimestamp(column);
    }

    public java.sql.Date getSqlDate(String column) {
        return selectColumn(column, RowReader.dateReader());
    }

    public Time getTime(String column) {
        return selectColumn(column, RowReader.timeReader());
    }

    public Timestamp getTimestamp(String column) {
        return selectColumn(column, RowReader.timestampReader());
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

    private int columnIndex(String columnName) {
        for (int i = 0; i < columns.size(); i++) {
            if (columnName.equals(columns.get(i).getName())) {
                return i;
            }
        }

        return -1;
    }

    protected <T> T ensureAtMostOneRow(SelectStatementBuilder<T> builder, T defaultValue) {

        List<T> data = builder.select(2);
        switch (data.size()) {
            case 0:
                return defaultValue;
            case 1:
                return data.get(0);
            default:
                throw new IllegalArgumentException("At most one row expected in the result");
        }
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
            table.quotationStrategy = quotingSqlIdentifiers && quoteSymbol != null
                    ? IdentifierQuotationStrategy.forQuoteSymbol(quoteSymbol)
                    : IdentifierQuotationStrategy.noQuote();

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

                if(types.isEmpty()) {
                    throw new RuntimeException("Table '" + table.getName() + "' is not found in DB");
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

        public Builder quoteSqlIdentifiers(boolean shouldQuote) {
            this.quotingSqlIdentifiers = shouldQuote;
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
