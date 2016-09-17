package io.bootique.jdbc.test;

import java.util.Collection;

public class WhereBuilder extends SQLBuilder {

    protected int whereCount;

    protected WhereBuilder(DBManager dbHelper, StringBuilder sqlBuffer,
            Collection<Object> bindings, Collection<Integer> bindingTypes) {
        super(dbHelper, sqlBuffer, bindings, bindingTypes);
        sqlBuffer.append(" where ");
    }

    public WhereBuilder and(String column, Object value) {
        return and(column, value, NO_TYPE);
    }

    public WhereBuilder and(String column, Object value, int valueType) {

        if (whereCount++ > 0) {
            sqlBuffer.append(" and ");
        }

        sqlBuffer.append(dbHelper.quote(column)).append(" = ?");
        initBinding(value, valueType);

        return this;
    }

    public WhereBuilder or(String column, Object value) {
        return or(column, value, NO_TYPE);
    }

    public WhereBuilder or(String column, Object value, int valueType) {
        if (whereCount++ > 0) {
            sqlBuffer.append(" or ");
        }

        sqlBuffer.append(dbHelper.quote(column)).append(" = ?");
        initBinding(value, valueType);

        return this;
    }

}
