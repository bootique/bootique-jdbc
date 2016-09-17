package io.bootique.jdbc.test;

public class UpdateBuilder extends SQLBuilder {

    protected int setCount;

    protected UpdateBuilder(DBManager dbHelper, String tableName) {
        super(dbHelper);
        sqlBuffer.append("update ").append(dbHelper.quote(tableName)).append(" set ");
    }

    public UpdateBuilder set(String column, Object value) {
        return set(column, value, NO_TYPE);
    }

    public UpdateBuilder set(String column, Object value, int valueType) {
        if (setCount++ > 0) {
            sqlBuffer.append(", ");
        }

        sqlBuffer.append(dbHelper.quote(column)).append(" = ?");
        initBinding(value, valueType);
        return this;
    }

    public WhereBuilder where(String column, Object value) {
        return where(column, value, NO_TYPE);
    }

    public WhereBuilder where(String column, Object value, int valueType) {
        WhereBuilder where = new WhereBuilder(dbHelper, sqlBuffer, bindings, bindingTypes);
        where.and(column, value, valueType);
        return where;
    }
}
