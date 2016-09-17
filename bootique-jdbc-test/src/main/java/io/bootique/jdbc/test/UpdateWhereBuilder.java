package io.bootique.jdbc.test;

public class UpdateWhereBuilder {

    protected UpdatingSqlContext context;
    protected int whereCount;

    protected UpdateWhereBuilder(UpdatingSqlContext context) {
        this.context = context;
        context.append(" WHERE ");
    }

    public int execute() {
        return context.execute();
    }

    public UpdateWhereBuilder and(String column, Object value) {
        return and(column, value, Column.NO_TYPE);
    }

    public UpdateWhereBuilder and(String column, Object value, int valueType) {

        if (whereCount++ > 0) {
            context.append(" AND ");
        }

        context.appendIdentifier(column).append(" = ?");
        context.addBinding(new Column(column, valueType), value);

        return this;
    }

    public UpdateWhereBuilder or(String column, Object value) {
        return or(column, value, Column.NO_TYPE);
    }

    public UpdateWhereBuilder or(String column, Object value, int valueType) {
        if (whereCount++ > 0) {
            context.append(" OR ");
        }

        context.appendIdentifier(column).append(" = ?");
        context.addBinding(new Column(column, valueType), value);

        return this;
    }

}
