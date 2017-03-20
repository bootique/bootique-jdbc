package io.bootique.jdbc.test;

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
import java.util.Objects;
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
    protected BindingValueToStringConverter valueToStringConverter;

    public DefaultDatabaseChannel(DataSource dataSource, String identifierQuote) {
        LOGGER.debug("Test DatabaseChannel opened...");
        this.dataSource = dataSource;
        this.identifierQuote = Objects.requireNonNull(identifierQuote);
        this.valueToStringConverter = new BindingValueToStringConverter();
    }

    @Override
    public String getIdentifierQuote() {
        return identifierQuote;
    }

    @Override
    public <T> List<T> select(String sql, long maxRows, Function<ResultSet, T> rowReader) {
        try {
            return selectWithExceptions(sql, maxRows, rowReader);
        } catch (SQLException e) {
            throw new RuntimeException("Error running SQL: " + sql, e);
        }
    }

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

    @Override
    public int update(String sql, List<Binding> bindings) {
        try {
            return updateWithExceptions(sql, bindings);
        } catch (SQLException e) {
            throw new RuntimeException("Error running SQL: " + sql, e);
        }
    }

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
