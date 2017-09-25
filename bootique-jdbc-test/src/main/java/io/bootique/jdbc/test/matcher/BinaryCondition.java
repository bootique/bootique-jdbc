package io.bootique.jdbc.test.matcher;

/**
 * @since 0.24
 */
class BinaryCondition {

    enum Comparision {

        eq {
            @Override
            public String getSqlOperator() {
                return "=";
            }
        };

        public abstract String getSqlOperator();
    }

    private String column;
    private Object value;
    private Comparision operator;

    public BinaryCondition(String column, Comparision operator, Object value) {
        this.column = column;
        this.value = value;
        this.operator = operator;
    }

    public String getColumn() {
        return column;
    }

    public Object getValue() {
        return value;
    }

    public Comparision getOperator() {
        return operator;
    }
}
