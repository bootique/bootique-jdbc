package io.bootique.jdbc.test;

import io.bootique.jdbc.test.runtime.DatabaseChannelFactory;
import io.bootique.test.BQTestRuntime;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;

public interface DatabaseChannel {

    static DatabaseChannel get(BQTestRuntime runtime) {
        return runtime.getRuntime().getInstance(DatabaseChannelFactory.class).getChannel();
    }

    static DatabaseChannel get(BQTestRuntime runtime, String dataSourceName) {
        return runtime.getRuntime().getInstance(DatabaseChannelFactory.class).getChannel(dataSourceName);
    }

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

    void close();
}
