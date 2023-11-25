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
import io.bootique.jdbc.test.jdbc.ArrayReader;
import io.bootique.jdbc.test.jdbc.ExecStatementBuilder;
import io.bootique.jdbc.test.jdbc.SelectStatementBuilder;
import io.bootique.jdbc.test.matcher.TableMatcher;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static java.util.Arrays.asList;

/**
 * JDBC utility class for manipulating and analyzing data in a single DB table. Used to load, clean up and match test
 * data.
 *
 * @deprecated since 3.0.M1, as we are we phasing out JUnit 4 support in favor of JUnit 5
 */
@Deprecated
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
     */
    public IdentifierQuotationStrategy getQuotationStrategy() {
        return quotationStrategy;
    }

    /**
     * @return a new {@link ExecStatementBuilder} object that assists in creating and executing a PreparedStatement
     * using policies specified for this table.
     */
    public ExecStatementBuilder execStatement() {
        return getChannel().execStatement().quoteIdentifiersWith(quotationStrategy);
    }

    /**
     * @return returns an immutable list of columns.
     */
    public List<Column> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    /**
     * @param name column name
     * @return a column for name.
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
     */
    public UpdateSetBuilder update() {
        ExecStatementBuilder builder = execStatement()
                .append("update ")
                .appendIdentifier(name)
                .append(" set ");

        return new UpdateSetBuilder(builder);
    }

    public DeleteBuilder delete() {
        ExecStatementBuilder builder = execStatement()
                .append("delete from ")
                .appendIdentifier(name);

        return new DeleteBuilder(builder);
    }

    public int deleteAll() {
        return delete().exec();
    }

    /**
     * @param columns an array of columns that is a subset of the table columns.
     * @return a builder for insert query.
     */
    public InsertBuilder insertColumns(String... columns) {
        return insertColumns(toColumnsList(columns));
    }

    /**
     * Returns a builder object to assemble a data set matching this table structure either from CSV-like strings,
     * or from a CSV file resource.
     *
     * @return a builder of a {@link io.bootique.jdbc.test.dataset.TableDataSet}.
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
     */
    public TableMatcher matcher() {
        return new TableMatcher(this);
    }

    /**
     * @deprecated since 2.0. This API is superceded either by the {@link #matcher()} or by {@link #selectColumns(String...)}.
     */
    @Deprecated
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
     * @deprecated since 2.0. This API is superceded either by the {@link #matcher()} or by {@link #selectColumns(String...)}.
     */
    @Deprecated
    public List<Object[]> select() {
        return selectColumns(this.columns).select();
    }

    /**
     * @deprecated since 2.0. This API is superceded either by the {@link #matcher()} or by {@link #selectColumns(String...)}.
     */
    @Deprecated
    public Object[] selectOne() {
        return selectColumns(this.columns).selectOne(null);
    }

    /**
     * @since 2.0
     */
    public SelectBuilder<Object[]> selectColumns(String... columns) {
        return selectColumns(toColumnsList(columns));
    }

    /**
     * @since 2.0
     */
    public SelectBuilder<Object[]> selectAllColumns() {
        return selectColumns(this.columns);
    }

    /**
     * @since 2.0
     */
    public SelectBuilder<Object[]> selectColumns(List<Column> columns) {
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("No columns");
        }

        SelectStatementBuilder<Object[]> builder = getChannel()
                .selectStatement()
                .reader(ArrayReader.create(columns.toArray(new Column[0])))
                .quoteIdentifiersWith(quotationStrategy)
                .append("select ");

        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(i);

            if (i > 0) {
                builder.append(", ");
            }
            builder.appendIdentifier(col.getName());
        }

        builder.append(" from ").appendIdentifier(name);
        return new SelectBuilder<>(builder);
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

    public static class Builder {

        private final Table table;
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

                if (types.isEmpty()) {
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
