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

import io.bootique.jdbc.junit5.RowReader;
import io.bootique.jdbc.junit5.Table;
import io.bootique.jdbc.junit5.metadata.DbMetadata;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
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
     * @param rowReader a function that converts a ResultSet row into an object.
     * @param <T>       the type of objects read by returned statement builder.
     * @return a new {@link SelectStatementBuilder} object that assists in creating and running a selecting
     * PreparedStatement.
     */
    public <T> SelectStatementBuilder<T> selectStatement(RowReader<T> rowReader) {
        return new SelectStatementBuilder(
                rowReader,
                this,
                objectValueConverter,
                valueToStringConverter,
                identifierQuoter);
    }
}
