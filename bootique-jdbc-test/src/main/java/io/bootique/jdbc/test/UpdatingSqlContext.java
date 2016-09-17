package io.bootique.jdbc.test;

import java.util.List;

public class UpdatingSqlContext {

    protected JdbcStore store;
    protected List<Binding> bindings;
    protected StringBuilder sqlBuffer;

    protected UpdatingSqlContext(JdbcStore store, StringBuilder sqlBuffer, List<Binding> bindings) {
        this.store = store;
        this.bindings = bindings;
        this.sqlBuffer = sqlBuffer;
    }

    public int execute() {
        return store.execute(sqlBuffer.toString(), bindings);
    }

    public UpdatingSqlContext append(String sql) {
        sqlBuffer.append(sql);
        return this;
    }

    public UpdatingSqlContext appendIdentifier(String sqlIdentifier) {
        sqlBuffer.append(store.quote(sqlIdentifier));
        return this;
    }

    public void addBinding(Column column, Object value) {
        bindings.add(new Binding(column, value));
    }
}
