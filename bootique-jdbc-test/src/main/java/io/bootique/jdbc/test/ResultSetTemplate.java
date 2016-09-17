package io.bootique.jdbc.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

abstract class ResultSetTemplate<T> {

    protected DBManager parent;

    public ResultSetTemplate(DBManager parent) {
        this.parent = parent;
    }

    protected abstract T readResultSet(ResultSet rs, String sql) throws SQLException;

    T execute(String sql) {
        try {
            return executeWithExceptions(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error running SQL: " + sql, e);
        }
    }

    protected T executeWithExceptions(String sql) throws SQLException {
        Logger.log(sql);
        try (Connection c = parent.getConnection();) {
            try (PreparedStatement st = c.prepareStatement(sql);) {
                try (ResultSet rs = st.executeQuery();) {
                    return readResultSet(rs, sql);
                }
            }
        }
    }
}
