package io.bootique.jdbc.test;

public class DeleteBuilder extends SQLBuilder {

    protected DeleteBuilder(DBManager dbHelper, String tableName) {
        super(dbHelper);
        sqlBuffer.append("delete from ").append(dbHelper.quote(tableName));
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
