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

package io.bootique.jdbc.junit5;

import io.bootique.jdbc.junit5.connector.*;
import io.bootique.jdbc.junit5.dataset.CsvDataSetBuilder;
import io.bootique.jdbc.junit5.dataset.TableDataSet;
import io.bootique.jdbc.junit5.matcher.TableMatcher;
import io.bootique.jdbc.junit5.metadata.DbColumnMetadata;
import io.bootique.jdbc.junit5.metadata.DbTableMetadata;

/**
 * JDBC utility class for manipulating and analyzing data in a single DB table. Used to load, clean up and match test
 * data.
 *
 * @since 2.0
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
     * @return a new {@link UpdateSetBuilder}.
     */
    public UpdateSetBuilder update() {
        ExecStatementBuilder builder = execStatement()
                .append("update ")
                .appendTableName(metadata.getName())
                .append(" set ");

        return new UpdateSetBuilder(builder);
    }

    public DeleteBuilder delete() {
        ExecStatementBuilder builder = execStatement()
                .append("delete from ")
                .appendTableName(metadata.getName());

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
        return insertColumns(toColumnMetadata(columns));
    }

    /**
     * Returns a builder object to assemble a data set matching this table structure either from CSV-like strings,
     * or from a CSV file resource.
     *
     * @return a builder of a {@link TableDataSet}.
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
     * @param columns an array of columns to select
     * @since 2.0.B1
     */
    public SelectBuilder<Object[]> selectColumns(String... columns) {
        return selectColumns(toColumnMetadata(columns));
    }

    /**
     * @since 2.0.B1
     */
    public SelectBuilder<Object[]> selectAllColumns() {
        return selectColumns(metadata.getColumns());
    }

    /**
     * @since 2.0.B1
     */
    public SelectBuilder<Object[]> selectColumns(DbColumnMetadata... columns) {

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

        builder.append(" from ").appendTableName(metadata.getName());
        return new SelectBuilder<>(builder);
    }


    protected DbColumnMetadata[] toColumnMetadata(String... columns) {
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
}
