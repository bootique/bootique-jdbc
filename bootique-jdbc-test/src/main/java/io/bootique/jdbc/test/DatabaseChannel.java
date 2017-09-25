package io.bootique.jdbc.test;

import io.bootique.BQRuntime;
import io.bootique.jdbc.test.jdbc.ExecStatementBuilder;
import io.bootique.jdbc.test.jdbc.RowReader;
import io.bootique.jdbc.test.jdbc.SelectStatementBuilder;
import io.bootique.jdbc.test.jdbc.StatementBuilder;
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

    /**
     * @param sql
     * @param maxRows
     * @param rowReader
     * @param <T>
     * @return
     * @deprecated since 0.24 as the statements are built and executed by {@link StatementBuilder}.
     */
    <T> List<T> select(String sql, long maxRows, Function<ResultSet, T> rowReader);

    /**
     * @param sql
     * @param bindings
     * @return
     * @deprecated since 0.24 as the statements are built and executed by {@link StatementBuilder}.
     */
    default int update(String sql, Binding... bindings) {
        return update(sql, asList(bindings));
    }

    /**
     * @param sql
     * @param bindings
     * @return
     * @deprecated since 0.24 as the statements are built and executed by {@link StatementBuilder}.
     */
    int update(String sql, List<Binding> bindings);

    Connection getConnection();

    void close();

    /**
     * Converts java types into proper sql types
     *
     * @param value an object to be converted
     * @return converted {@code value}
     * @since 0.15
     * @deprecated since 0.24 as value conversions occur inside {@link StatementBuilder}.
     */
    Object convert(Object value);

    /**
     * @return a new {@link ExecStatementBuilder} object that assists in creating and executing a PreparedStatement.
     * @since 0.24
     */
    ExecStatementBuilder execStatement();

    /**
     * @param rowReader a function that converts a ResultSet row into an object.
     * @param <T>       the type of objects read by returned statement builder.
     * @return a new {@link SelectStatementBuilder} object that assists in creating and running a selecting
     * PreparedStatement.
     * @since 0.24
     */
    <T> SelectStatementBuilder<T> selectStatement(RowReader<T> rowReader);
}
