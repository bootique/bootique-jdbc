package io.bootique.jdbc.test;

import java.util.List;

public class UpdatingSqlContext {

    protected DatabaseChannel channel;
    protected List<Binding> bindings;
    protected StringBuilder sqlBuffer;
    protected IdentifierQuotationStrategy quotationStrategy;

    protected UpdatingSqlContext(DatabaseChannel channel,
                                 IdentifierQuotationStrategy quotationStrategy,
                                 StringBuilder sqlBuffer,
                                 List<Binding> bindings) {
        this.channel = channel;
        this.bindings = bindings;
        this.sqlBuffer = sqlBuffer;
        this.quotationStrategy = quotationStrategy;
    }

    public int execute() {
        return channel.update(getSQL(), bindings);
    }

    protected String getSQL() {
        return sqlBuffer.toString();
    }

    public UpdatingSqlContext append(String sql) {
        sqlBuffer.append(sql);
        return this;
    }

    public UpdatingSqlContext appendIdentifier(String sqlIdentifier) {
        sqlBuffer.append(quotationStrategy.quoted(sqlIdentifier));
        return this;
    }

    public void addBinding(Column column, Object value) {
        bindings.add(new Binding(column, channel.convert(value)));
    }
}
