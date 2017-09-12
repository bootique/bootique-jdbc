package io.bootique.jdbc.test.jdbc;

import io.bootique.jdbc.test.BindingValueToStringConverter;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.IdentifierQuotationStrategy;
import io.bootique.jdbc.test.ObjectValueConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @since 0.24
 */
public class ExecStatementBuilder extends StatementBuilder<ExecStatementBuilder> {

    public ExecStatementBuilder(
            DatabaseChannel channel,
            ObjectValueConverter objectValueConverter,
            BindingValueToStringConverter valueToStringConverter,
            IdentifierQuotationStrategy quotationStrategy) {
        super(channel, objectValueConverter, valueToStringConverter, quotationStrategy);
    }

    public int exec(String sql) {
        return append(sql).exec();
    }

    public int exec() {

        String sql = getSql();
        log(sql, bindings);

        try {
            return execWithExceptions(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error running updating SQL: " + sql, e);
        }
    }

    protected int execWithExceptions(String sql) throws SQLException {

        try (Connection c = channel.getConnection();) {

            int count;
            try (PreparedStatement st = c.prepareStatement(sql)) {
                bind(st);
                count = st.executeUpdate();
            }

            c.commit();
            return count;
        }
    }
}
