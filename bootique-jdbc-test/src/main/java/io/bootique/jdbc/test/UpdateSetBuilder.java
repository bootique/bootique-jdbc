package io.bootique.jdbc.test;

import io.bootique.jdbc.test.jdbc.ExecStatementBuilder;

public class UpdateSetBuilder {

    protected ExecStatementBuilder builder;
    protected int setCount;

    protected UpdateSetBuilder(ExecStatementBuilder builder) {
        this.builder = builder;
    }

    /**
     * @return the number of updated records.
     * @since 0.24
     */
    public int exec() {
        return builder.exec();
    }


    public UpdateSetBuilder set(String column, Object value) {
        return set(column, value, Column.NO_TYPE);
    }

    public UpdateSetBuilder set(String column, Object value, int valueType) {
        if (setCount++ > 0) {
            builder.append(", ");
        }

        builder.appendIdentifier(column)
                .append(" = ")
                .appendBinding(column, valueType, value);
        return this;
    }

    public UpdateWhereBuilder where(String column, Object value) {
        return where(column, value, Column.NO_TYPE);
    }

    public UpdateWhereBuilder where(String column, Object value, int valueType) {
        UpdateWhereBuilder where = new UpdateWhereBuilder(builder);
        where.and(column, value, valueType);
        return where;
    }
}
