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

package io.bootique.jdbc.junit5.connector;

import io.bootique.jdbc.junit5.RowConverter;
import io.bootique.jdbc.junit5.Table;
import io.bootique.jdbc.junit5.metadata.DbMetadata;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A wrapper around a DB DataSource that provides access to DB metadata, {@link Table} API and various query builders.
 *
 * @since 2.0
 */
public class DbConnector {

    protected DataSource dataSource;
    protected DbMetadata metadata;

    protected IdentifierQuoter identifierQuoter;
    protected BindingValueToStringConverter valueToStringConverter;
    protected ObjectValueConverter objectValueConverter;

    protected Map<String, Table> tables;

    public DbConnector(DataSource dataSource, DbMetadata metadata) {

        this.dataSource = Objects.requireNonNull(dataSource);
        this.metadata = Objects.requireNonNull(metadata);

        this.valueToStringConverter = new BindingValueToStringConverter();
        this.objectValueConverter = new ObjectValueConverter();

        this.identifierQuoter = metadata.shouldQuoteIdentifiers()
                ? IdentifierQuoter.forQuoteSymbol(metadata.getIdentifierQuote())
                : IdentifierQuoter.noQuote();

        this.tables = new ConcurrentHashMap<>();
    }

    public IdentifierQuoter getIdentifierQuoter() {
        return identifierQuoter;
    }

    public Table getTable(String tableName) {
        return tables.computeIfAbsent(tableName, tn -> new Table(this, metadata.getTable(tn)));
    }

    public DbMetadata getMetadata() {
        return metadata;
    }

    public Connection getConnection() {

        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Error opening connection", e);
        }

        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {

            try {
                connection.close();
            } catch (SQLException ignored) {
            }

            // TODO: this catch is suspect ... It may return connection in a closed state
        }
        return connection;
    }

    /**
     * @return a new {@link ExecStatementBuilder} object that assists in creating and executing a PreparedStatement.
     */
    public ExecStatementBuilder execStatement() {
        return new ExecStatementBuilder(
                this,
                objectValueConverter,
                valueToStringConverter,
                identifierQuoter);
    }

    /**
     * @return a new {@link SelectStatementBuilder} object that assists in creating and running a selecting
     * PreparedStatement.
     * @since 2.0.B1
     */
    public SelectStatementBuilder<Object[]> selectStatement() {
        return new SelectStatementBuilder(
                ArrayReader.create(),
                RowConverter.identity(),
                this,
                objectValueConverter,
                valueToStringConverter,
                identifierQuoter);
    }
}
