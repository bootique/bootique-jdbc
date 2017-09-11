package io.bootique.jdbc.test;

import io.bootique.jdbc.test.jdbc.ExecStatementBuilder;

public class UpdateWhereBuilder {

    protected ExecStatementBuilder builder;
    protected int whereCount;

    protected UpdateWhereBuilder(ExecStatementBuilder builder) {
        this.builder = builder;
    }

    /**
     * @return the number of updated records.
     * @deprecated since 0.24 in favor for {@link #exec()}.
     */
    public int execute() {
        return builder.exec();
    }

    /**
     * @return the number of updated records.
     * @since 0.24
     */
    public int exec() {
        return builder.exec();
    }

    public UpdateWhereBuilder and(String column, Object value) {
        return and(column, value, Column.NO_TYPE);
    }

    public UpdateWhereBuilder and(String column, Object value, int valueType) {

        if (whereCount++ > 0) {
            builder.append(" AND ");
        } else {
            builder.append(" WHERE ");
        }

        builder.appendIdentifier(column)
                .append(" = ")
                .appendBinding(column, valueType, value);

        return this;
    }

    public UpdateWhereBuilder or(String column, Object value) {
        return or(column, value, Column.NO_TYPE);
    }

    public UpdateWhereBuilder or(String column, Object value, int valueType) {
        if (whereCount++ > 0) {
            builder.append(" OR ");
        }

        builder.appendIdentifier(column)
                .append(" = ")
                .appendBinding(column, valueType, value);

        return this;
    }

}
