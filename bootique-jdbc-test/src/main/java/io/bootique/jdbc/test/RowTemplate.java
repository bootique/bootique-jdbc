package io.bootique.jdbc.test;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A JDBC template for reading a single row from the database.
 */
abstract class RowTemplate<T> extends ResultSetTemplate<T> {

    public RowTemplate(JdbcStore parent) {
        super(parent);
    }

    abstract T readRow(ResultSet rs, String sql) throws SQLException;

    @Override
    protected T readResultSet(ResultSet rs, String sql) throws SQLException {
        if (rs.next()) {

            T row = readRow(rs, sql);

            if (rs.next()) {
                throw new SQLException("More than one result for sql: " + sql);
            }

            return row;
        } else {
            throw new SQLException("No results for sql: " + sql);
        }
    }
}
