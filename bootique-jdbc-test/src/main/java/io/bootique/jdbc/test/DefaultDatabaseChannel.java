package io.bootique.jdbc.test;

import io.bootique.jdbc.test.jdbc.ExecStatementBuilder;
import io.bootique.jdbc.test.jdbc.RowReader;
import io.bootique.jdbc.test.jdbc.SelectStatementBuilder;
import io.bootique.jdbc.test.jdbc.StatementBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @Deprecated
    public <T> List<T> select(String sql, long maxRows, Function<ResultSet, T> rowReader) {
        try {
            return selectWithExceptions(sql, maxRows, rowReader);
        } catch (SQLException e) {
            throw new RuntimeException("Error running SQL: " + sql, e);
        }
    }

    @Deprecated
    protected <T> List<T> selectWithExceptions(String sql, long maxRows, Function<ResultSet, T> rowReader)
            throws SQLException {

        List<T> result = new ArrayList<>();

        logPreparedStatement(sql, Collections.emptyList());
        try (Connection c = getConnection();) {
            try (PreparedStatement st = c.prepareStatement(sql);) {
                try (ResultSet rs = st.executeQuery();) {

                    while (rs.next() && result.size() < maxRows) {
                        result.add(rowReader.apply(rs));
                    }
                }
            }
        }

        return result;
    }

    /**
     * @param sql
     * @param bindings
     * @return update count.
     * @deprecated since 0.24 as the statements are built and executed by {@link StatementBuilder}.
     */
    @Override
    @Deprecated
    public int update(String sql, List<Binding> bindings) {
        try {
            return updateWithExceptions(sql, bindings);
        } catch (SQLException e) {
            throw new RuntimeException("Error running SQL: " + sql, e);
        }
    }

    /**
     * @param sql
     * @param bindings
     * @return update count.
     * @throws SQLException
     * @deprecated since 0.24 as the statements are built and executed by {@link StatementBuilder}.
     */
    @Deprecated
    protected int updateWithExceptions(String sql, List<Binding> bindings) throws SQLException {
        logPreparedStatement(sql, bindings);

        try (Connection c = getConnection();) {

            int count;
            try (PreparedStatement st = c.prepareStatement(sql);) {

                for (int i = 0; i < bindings.size(); i++) {
                    bindings.get(i).bind(st, i);
                }

                count = st.executeUpdate();
            }

            c.commit();
            return count;
        }
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
    @Deprecated
    public Object convert(Object value) {
        return objectValueConverter.convert(value);
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

    /**
     * @param sql
     * @param bindings
     * @deprecated since 0.24 as the statements are built and executed by {@link StatementBuilder}.
     */
    @Deprecated
    protected void logPreparedStatement(String sql, List<Binding> bindings) {

        if (!LOGGER.isInfoEnabled()) {
            return;
        }

        if (bindings.isEmpty()) {
            LOGGER.info(sql);
            return;
        }

        String toLog = bindings
                .stream()
                .map(b -> b.getColumn().getName() + "->" + valueToStringConverter.convert(b.getValue()))
                .collect(Collectors.joining(", ", sql + " [", "]"));

        LOGGER.info(toLog);
    }
}
