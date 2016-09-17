package io.bootique.jdbc.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;

public interface DatabaseChannel {

    default Table.Builder newTable(String name) {
        return Table.builder(this, name);
    }

    /**
     * Quotes a SQL identifier as appropriate for the given DB. This implementation returns the identifier unchanged,
     * while subclasses may implement a custom quoting strategy.
     */
    default String quote(String sqlIdentifier) {
        return sqlIdentifier;
    }

    <T> List<T> select(String sql, long maxRows, Function<ResultSet, T> rowReader);

    default int update(String sql, Binding... bindings) {
        return update(sql, asList(bindings));
    }

    int update(String sql, List<Binding> bindings);

    Connection getConnection();
}
