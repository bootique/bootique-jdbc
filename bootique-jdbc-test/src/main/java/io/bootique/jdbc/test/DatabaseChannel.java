package io.bootique.jdbc.test;

import io.bootique.BQRuntime;
import io.bootique.jdbc.test.jdbc.ExecStatementBuilder;
import io.bootique.jdbc.test.jdbc.RowReader;
import io.bootique.jdbc.test.jdbc.SelectStatementBuilder;
import io.bootique.jdbc.test.runtime.DatabaseChannelFactory;

import java.sql.Connection;

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

    Connection getConnection();

    void close();

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
