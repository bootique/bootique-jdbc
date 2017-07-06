package io.bootique.jdbc.test;

public class UpdateSetBuilder {

    protected UpdatingSqlContext context;
    protected int setCount;

    protected UpdateSetBuilder(UpdatingSqlContext context) {
        this.context = context;
    }

    public int execute() {
        return context.execute();
    }

    public UpdateSetBuilder set(String column, Object value) {
        return set(column, value, Column.NO_TYPE);
    }

    public UpdateSetBuilder set(String column, Object value, int valueType) {
        if (setCount++ > 0) {
            context.append(", ");
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
