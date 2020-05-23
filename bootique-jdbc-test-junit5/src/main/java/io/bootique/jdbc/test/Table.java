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

import io.bootique.jdbc.test.connector.DbConnector;
import io.bootique.jdbc.test.dataset.CsvDataSetBuilder;
import io.bootique.jdbc.test.jdbc.ExecStatementBuilder;
import io.bootique.jdbc.test.jdbc.RowReader;
import io.bootique.jdbc.test.jdbc.SelectStatementBuilder;
import io.bootique.jdbc.test.matcher.TableMatcher;
import io.bootique.jdbc.test.metadata.DbColumnMetadata;
import io.bootique.jdbc.test.metadata.DbTableMetadata;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC utility class for setting up and analyzing the DB data sets for a single table.
 * Table intentionally bypasses Cayenne stack.
 */
public class Table {

    protected DbConnector connector;
    protected DbTableMetadata metadata;

    public Table(DbConnector connector, DbTableMetadata metadata) {
        this.connector = connector;
        this.metadata = metadata;
    }

    public DbConnector getConnector() {
        return connector;
    }

    public DbTableMetadata getMetadata() {
        return metadata;
    }

    /**
     * @return a new {@link ExecStatementBuilder} object that assists in creating and executing a PreparedStatement
     * using policies specified for this table.
     */
    public ExecStatementBuilder execStatement() {
        return getConnector().execStatement();
    }

    /**
     * @param rowReader a function that converts a ResultSet row into an object.
     * @param <T>       the type of objects read by returned statement builder.
     * @return a new {@link SelectStatementBuilder} object that assists in creating and running a selecting
     * PreparedStatement using policies specified for this table.
     */
    public <T> SelectStatementBuilder<T> selectStatement(RowReader<T> rowReader) {
        return getConnector().selectStatement(rowReader);
    }

    /**
     * Update table statement
     *
     * @return {@link UpdateSetBuilder}
     */
    public UpdateSetBuilder update() {
        ExecStatementBuilder builder = execStatement()
                .append("update ")
                .appendTableName(metadata.getName())
                .append(" set ");

        return new UpdateSetBuilder(builder);
    }

    public UpdateWhereBuilder delete() {
        ExecStatementBuilder builder = execStatement()
                .append("delete from ")
                .appendTableName(metadata.getName());

        return new UpdateWhereBuilder(builder);
    }

    public int deleteAll() {
        return delete().exec();
    }

    /**
     * @param columns an array of columns that is a subset of the table columns.
     * @return a builder for insert query.
     */
    public InsertBuilder insertColumns(String... columns) {
        return insertColumns(toColumnsArray(columns));
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
        insertColumns(metadata.getColumns()).values(values).exec();
        return this;
    }

    public InsertBuilder insertColumns(DbColumnMetadata[] columns) {
        if (columns == null) {
            throw new NullPointerException("Null columns");
        }

        if (columns.length == 0) {
            throw new IllegalArgumentException("No columns in the list");
        }

        return new InsertBuilder(execStatement(), metadata.getName(), columns);
    }

    /**
     * @return a new instance of {@link TableMatcher} for this table that allows to make assertions about the table data.
     */
    public TableMatcher matcher() {
        return new TableMatcher(this);
    }

    /**
     * Performs select operation against the table, returning result as a map using provided unique column as a key.
     *
     * @param mapColumn the name of a unique column to use as a map key.
     * @return a map using provided unique column as a key.
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
        return selectColumns(metadata.getColumns());
    }

    /**
     * Selects a single row from the mapped table.
     */
    public Object[] selectOne() {
        return ensureAtMostOneRow(selectColumnsBuilder(metadata.getColumns()), null);
    }

    /**
     * @param columns an array of columns to select.
     * @return a List of Object[] where each array represents a row in the underlying table made of columns requested
     * in this method.
     */
    public List<Object[]> selectColumns(String... columns) {
        return selectColumns(toColumnsArray(columns));
    }

    public List<Object[]> selectColumns(DbColumnMetadata... columns) {
        return selectColumnsBuilder(columns).select(Long.MAX_VALUE);
    }

    protected SelectStatementBuilder<Object[]> selectColumnsBuilder(DbColumnMetadata... columns) {
        if (columns == null || columns.length == 0) {
            throw new IllegalArgumentException("No columns");
        }

        SelectStatementBuilder<Object[]> builder = this
                .selectStatement(RowReader.arrayReader(columns.length))
                .append("select ");

        for (int i = 0; i < columns.length; i++) {
            DbColumnMetadata col = columns[i];

            if (i > 0) {
                builder.append(", ");
            }
            builder.appendIdentifier(col.getName());
        }

        return builder.append(" from ").appendTableName(metadata.getName());
    }

    protected <T> T selectColumn(String columnName, RowReader<T> reader) {
        return selectColumn(columnName, reader, null);
    }

    protected <T> T selectColumn(String columnName, RowReader<T> reader, T defaultValue) {
        SelectStatementBuilder<T> builder = selectStatement(reader)
                .append("select ")
                .appendIdentifier(columnName)
                .append(" from ")
                .appendTableName(metadata.getName());
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

    protected DbColumnMetadata[] toColumnsArray(String... columns) {
        if (columns == null) {
            throw new NullPointerException("Null columns");
        }

        if (columns.length == 0) {
            throw new IllegalArgumentException("No columns in the list");
        }

        DbColumnMetadata[] columnsArray = new DbColumnMetadata[columns.length];
        for (int i = 0; i < columns.length; i++) {
            columnsArray[i] = metadata.getColumn(columns[i]);
        }

        return columnsArray;
    }

    private int columnIndex(String columnName) {
        DbColumnMetadata[] columns = metadata.getColumns();

        for (int i = 0; i < columns.length; i++) {
            if (columnName.equals(columns[i].getName())) {
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
}
