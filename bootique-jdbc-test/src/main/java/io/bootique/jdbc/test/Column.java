package io.bootique.jdbc.test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Column {

    static final int NO_TYPE = Integer.MIN_VALUE;

    private String name;
    private int type;

    public Column(String name) {
        this(name, NO_TYPE);
    }

    public Column(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    protected boolean typeUnknown() {
        return type == NO_TYPE;
    }

    public void bind(PreparedStatement statement, int position, Object value) {

        try {
            if (value == null) {
                bindNull(statement, position);
            } else {
                bindNotNull(statement, position, value);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error binding value for column '" + name + "'", e);
        }
    }

    protected void bindNull(PreparedStatement statement, int position) throws SQLException {

        int jdbcPosition = position + 1;
        int type = this.type;

        if (typeUnknown()) {
            type = statement.getParameterMetaData().getParameterType(jdbcPosition);
        }

        statement.setNull(jdbcPosition, type);
    }

    protected void bindNotNull(PreparedStatement statement, int position, Object value) throws SQLException {

        int jdbcPosition = position + 1;

        if (typeUnknown()) {
            statement.setObject(jdbcPosition, value);
        } else {
            statement.setObject(jdbcPosition, value, type);
        }
    }


}
