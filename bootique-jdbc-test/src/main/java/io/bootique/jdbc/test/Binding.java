package io.bootique.jdbc.test;

import java.sql.PreparedStatement;

public class Binding {

    private Column column;
    private Object value;

    public Binding(Column column, Object value) {
        this.column = column;
        this.value = value;
    }

    protected void bind(PreparedStatement statement, int position) {
        column.bind(statement, position, value);
    }
}
