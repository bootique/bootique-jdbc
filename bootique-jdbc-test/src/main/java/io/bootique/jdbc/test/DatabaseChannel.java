package io.bootique.jdbc.test;

import io.bootique.BQRuntime;
import io.bootique.jdbc.test.runtime.DatabaseChannelFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;

public interface DatabaseChannel {

    static DatabaseChannel get(BQRuntime runtime) {
        return runtime.getInstance(DatabaseChannelFactory.class).getChannel();
    }

    static DatabaseChannel get(BQRuntime runtime, String dataSourceName) {
        return runtime.getInstance(DatabaseChannelFactory.class).getChannel(dataSourceName);
    }

    default Table.Builder newTable(String tableName) {
        return Table.builder(this, tableName);
    }

    /**
     * @return DB-specific identifier quotation symbol.
     * @since 0.14
     */
    String getIdentifierQuote();

    <T> List<T> select(String sql, long maxRows, Function<ResultSet, T> rowReader);

    default int update(String sql, Binding... bindings) {
        return update(sql, asList(bindings));
    }

    int update(String sql, List<Binding> bindings);

    Connection getConnection();

    void close();

    /**
     * Converts java types into proper sql types
     *
     * @param value an object to be converted
     * @return converted {@code value}
     * @since 0.15
     */
    Object convert(Object value);
}
