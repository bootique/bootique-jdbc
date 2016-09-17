package io.bootique.jdbc.test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * A helper class to run common DB operations during unit tests.
 *
 * @since 0.12
 */
public class JdbcStore {

    protected DataSource dataSource;

    public JdbcStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Quotes a SQL identifier as appropriate for the given DB. This implementation returns the identifier unchanged,
     * while subclasses may implement a custom quoting strategy.
     */
    protected String quote(String sqlIdentifier) {
        return sqlIdentifier;
    }

    public int execute(String sql, List<Binding> bindings) {
        try {
            return executeWithExceptions(sql, bindings);
        } catch (SQLException e) {
            throw new RuntimeException("Error running SQL: " + sql, e);
        }
    }

    protected int executeWithExceptions(String sql, List<Binding> bindings) throws SQLException {
        Logger.log(sql);

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

    public Connection getConnection() {
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

}
