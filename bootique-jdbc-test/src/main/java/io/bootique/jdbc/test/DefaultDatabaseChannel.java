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

import io.bootique.jdbc.test.jdbc.ArrayReader;
import io.bootique.jdbc.test.jdbc.ExecStatementBuilder;
import io.bootique.jdbc.test.jdbc.SelectStatementBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A helper class to run common DB operations during unit tests.
 */
public class DefaultDatabaseChannel implements DatabaseChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDatabaseChannel.class);

    protected boolean closed;
    protected DataSource dataSource;
    protected String identifierQuote;
    protected IdentifierQuotationStrategy defaultIdentifierQuotationStrategy;
    protected BindingValueToStringConverter valueToStringConverter;
    protected ObjectValueConverter objectValueConverter;

    public DefaultDatabaseChannel(DataSource dataSource, String identifierQuote, boolean defaultQuoteIdentifiers) {
        LOGGER.debug("Test DatabaseChannel opened...");
        this.dataSource = dataSource;
        this.identifierQuote = identifierQuote;
        this.valueToStringConverter = new BindingValueToStringConverter();
        this.objectValueConverter = new ObjectValueConverter();

        this.defaultIdentifierQuotationStrategy = defaultQuoteIdentifiers
                ? IdentifierQuotationStrategy.forQuoteSymbol(identifierQuote)
                : IdentifierQuotationStrategy.noQuote();
    }

    @Override
    public String getIdentifierQuote() {
        return identifierQuote;
    }

    IdentifierQuotationStrategy getDefaultIdentifierQuotationStrategy() {
        return defaultIdentifierQuotationStrategy;
    }

    @Override
    public Connection getConnection() {

        if (closed) {
            throw new IllegalStateException("The channel is closed");
        }

        Connection connection = null;
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

    @Override
    public void close() {
        LOGGER.debug("Test DatabaseChannel closed...");
        this.closed = true;
    }

    @Override
    public ExecStatementBuilder execStatement() {
        return new ExecStatementBuilder(
                this,
                objectValueConverter,
                valueToStringConverter,
                defaultIdentifierQuotationStrategy);
    }

    @Override
    public SelectStatementBuilder<Object[]> selectStatement() {
        return new SelectStatementBuilder(
                ArrayReader.create(),
                RowConverter.identity(),
                this,
                objectValueConverter,
                valueToStringConverter,
                defaultIdentifierQuotationStrategy);
    }
}
