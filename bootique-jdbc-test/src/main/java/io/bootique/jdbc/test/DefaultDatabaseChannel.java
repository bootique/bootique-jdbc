package io.bootique.jdbc.test;

import io.bootique.jdbc.test.jdbc.ExecStatementBuilder;
import io.bootique.jdbc.test.jdbc.RowReader;
import io.bootique.jdbc.test.jdbc.SelectStatementBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A helper class to run common DB operations during unit tests.
 *
 * @since 0.12
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
    public <T> SelectStatementBuilder<T> selectStatement(RowReader<T> rowReader) {
        return new SelectStatementBuilder(
                rowReader,
                this,
                objectValueConverter,
                valueToStringConverter,
                defaultIdentifierQuotationStrategy);
    }
}
