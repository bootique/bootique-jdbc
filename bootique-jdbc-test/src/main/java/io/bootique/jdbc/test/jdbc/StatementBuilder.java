package io.bootique.jdbc.test.jdbc;

import io.bootique.jdbc.test.Binding;
import io.bootique.jdbc.test.BindingValueToStringConverter;
import io.bootique.jdbc.test.Column;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.IdentifierQuotationStrategy;
import io.bootique.jdbc.test.ObjectValueConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines API to build a SQL
 *
 * @since 0.24
 */
public abstract class StatementBuilder<T extends StatementBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatementBuilder.class);

    protected ObjectValueConverter objectValueConverter;
    protected BindingValueToStringConverter valueToStringConverter;
    protected IdentifierQuotationStrategy quotationStrategy;
    protected DatabaseChannel channel;

    protected List<Binding> bindings;
    protected StringBuilder sqlBuffer;

    public StatementBuilder(
            DatabaseChannel channel,
            ObjectValueConverter objectValueConverter,
            BindingValueToStringConverter valueToStringConverter,
            IdentifierQuotationStrategy quotationStrategy) {

        this.channel = channel;
        this.objectValueConverter = objectValueConverter;
        this.quotationStrategy = quotationStrategy;
        this.valueToStringConverter = valueToStringConverter;

        this.bindings = new ArrayList<>();
        this.sqlBuffer = new StringBuilder();
    }

    protected void bind(PreparedStatement statement) {
        for (int i = 0; i < bindings.size(); i++) {
            bindings.get(i).bind(statement, i);
        }
    }

    protected void log(String sql, List<Binding> bindings) {

        if (!LOGGER.isInfoEnabled()) {
            return;
        }

        if (bindings.isEmpty()) {
            LOGGER.info(sql);
            return;
        }

        String toLog = bindings
                .stream()
                .map(b -> b.getColumn().getName() + "->" + valueToStringConverter.convert(b.getValue()))
                .collect(Collectors.joining(", ", sql + " [", "]"));

        LOGGER.info(toLog);
    }

    protected String getSql() {
        return sqlBuffer.toString();
    }

    public T append(String sql) {
        sqlBuffer.append(sql);
        return (T) this;
    }

    public T appendIdentifier(String sqlIdentifier) {
        sqlBuffer.append(quotationStrategy.quoted(sqlIdentifier));
        return (T) this;
    }

    public T appendBinding(String columnName, int valueType, Object value) {
        return appendBinding(new Column(columnName, valueType), value);
    }

    public T appendBinding(Column column, Object value) {
        sqlBuffer.append("?");
        bindings.add(new Binding(column, objectValueConverter.convert(value)));
        return (T) this;
    }
}
