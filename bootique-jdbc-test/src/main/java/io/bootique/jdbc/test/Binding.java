package io.bootique.jdbc.test;

import java.sql.PreparedStatement;

public class Binding {

    private Column column;
    private Object value;

    public Binding(Column column, Object value) {
        this.column = column;
        this.value = value;
    }

    public void bind(PreparedStatement statement, int position) {
        column.bind(statement, position, value);
    }

    public Column getColumn() {
        return column;
    }

    public Object getValue() {
        return value;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
