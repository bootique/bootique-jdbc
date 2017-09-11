package io.bootique.jdbc.test.jdbc;

import io.bootique.jdbc.test.BindingValueToStringConverter;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.IdentifierQuotationStrategy;
import io.bootique.jdbc.test.ObjectValueConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.24
 */
public class SelectStatementBuilder<T> extends StatementBuilder<SelectStatementBuilder<T>> {

    private RowReader<T> rowReader;

    public SelectStatementBuilder(
            RowReader<T> rowReader,
            DatabaseChannel channel,
            ObjectValueConverter objectValueConverter,
            BindingValueToStringConverter valueToStringConverter,
            IdentifierQuotationStrategy quotationStrategy) {
        super(channel, objectValueConverter, valueToStringConverter, quotationStrategy);
        this.rowReader = rowReader;
    }

    public List<T> select(long maxRows) {

        String sql = getSql();
        log(sql, bindings);

        try {
            return selectWithExceptions(sql, maxRows);
        } catch (SQLException e) {
            throw new RuntimeException("Error running selecting SQL: " + sql, e);
        }
    }

    protected List<T> selectWithExceptions(String sql, long maxRows) throws SQLException {

        List<T> result = new ArrayList<>();

        try (Connection c = channel.getConnection()) {
            try (PreparedStatement st = c.prepareStatement(sql)) {

                bind(st);
                try (ResultSet rs = st.executeQuery()) {

                    while (rs.next() && result.size() < maxRows) {
                        result.add(rowReader.readRow(rs));
                    }
                }
            }
        }

        return result;
    }
}
