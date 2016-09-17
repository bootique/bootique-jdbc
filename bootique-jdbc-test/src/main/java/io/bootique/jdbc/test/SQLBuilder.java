package io.bootique.jdbc.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public abstract class SQLBuilder {

    static final int NO_TYPE = Integer.MIN_VALUE;

    protected DBManager dbHelper;
    protected Collection<Object> bindings;
    protected Collection<Integer> bindingTypes;
    protected StringBuilder sqlBuffer;

    protected SQLBuilder(DBManager dbHelper) {
        this(
                dbHelper,
                new StringBuilder(),
                new ArrayList<Object>(),
                new ArrayList<Integer>());
    }

    protected SQLBuilder(DBManager dbHelper, StringBuilder sqlBuffer,
                         Collection<Object> bindings,
                         Collection<Integer> bindingTypes) {
        this.dbHelper = dbHelper;
        this.bindings = bindings;
        this.bindingTypes = bindingTypes;
        this.sqlBuffer = sqlBuffer;
    }

    public int execute() {
        return new UpdateTemplate(dbHelper).execute(
                sqlBuffer.toString(),
                bindings,
                bindingTypes);
    }

    protected void initBinding(Object value, int type) {
        bindings.add(value);
        bindingTypes.add(type);
    }
}
