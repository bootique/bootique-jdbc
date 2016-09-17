package io.bootique.jdbc.test;

import java.util.List;

public class UpdatingSqlContext {

    protected DatabaseChannel channel;
    protected List<Binding> bindings;
    protected StringBuilder sqlBuffer;

    protected UpdatingSqlContext(DatabaseChannel channel, StringBuilder sqlBuffer, List<Binding> bindings) {
        this.channel = channel;
        this.bindings = bindings;
        this.sqlBuffer = sqlBuffer;
    }

    public int execute() {
        return channel.update(sqlBuffer.toString(), bindings);
    }

    public UpdatingSqlContext append(String sql) {
        sqlBuffer.append(sql);
        return this;
    }

    public UpdatingSqlContext appendIdentifier(String sqlIdentifier) {
        sqlBuffer.append(channel.quote(sqlIdentifier));
        return this;
    }

    public void addBinding(Column column, Object value) {
        bindings.add(new Binding(column, value));
    }
}
