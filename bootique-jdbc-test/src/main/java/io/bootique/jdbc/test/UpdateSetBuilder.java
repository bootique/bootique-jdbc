package io.bootique.jdbc.test;

import java.util.List;

public class UpdateSetBuilder {

    protected UpdatingSqlContext context;
    protected List<Binding> bindings;
    protected StringBuilder sqlBuffer;
    protected int setCount;

    protected UpdateSetBuilder(UpdatingSqlContext context) {
        this.context = context;
        this.bindings = bindings;
        this.sqlBuffer = sqlBuffer;
        sqlBuffer.append(" SET ");
    }

    public UpdateSetBuilder set(String column, Object value) {
        return set(column, value, Column.NO_TYPE);
    }

    public UpdateSetBuilder set(String column, Object value, int valueType) {
        if (setCount++ > 0) {
            sqlBuffer.append(", ");
        }

        context.appendIdentifier(column).append(" = ?");
        context.addBinding(new Column(column, valueType), value);
        return this;
    }

    public UpdateWhereBuilder where(String column, Object value) {
        return where(column, value, Column.NO_TYPE);
    }

    public UpdateWhereBuilder where(String column, Object value, int valueType) {
        UpdateWhereBuilder where = new UpdateWhereBuilder(context);
        where.and(column, value, valueType);
        return where;
    }
}
