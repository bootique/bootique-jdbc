package io.bootique.jdbc.test.matcher;

import io.bootique.jdbc.test.Table;
import io.bootique.jdbc.test.jdbc.RowReader;
import io.bootique.jdbc.test.jdbc.SelectStatementBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @since 0.24
 */
public class RowCountMatcher {

    private Table table;
    private List<BinaryCondition> conditions;

    public RowCountMatcher(Table table) {
        this.table = table;
    }

    public RowCountMatcher eq(String column, Object value) {
        getConditions().add(new BinaryCondition(column, BinaryCondition.Comparision.eq, value));
        return this;
    }

    public void assertHasRows(int expectedRowCount) {

        SelectStatementBuilder<Integer> builder = countStatement();
        int count = appendConditions(builder)
                .select(1)
                .get(0);

        assertEquals("Unexpected row count in the DB", expectedRowCount, count);
    }

    protected SelectStatementBuilder<Integer> countStatement() {
        return table.newSelectStatement(RowReader.intReader())
                .append("SELECT COUNT(*) FROM ")
                .appendIdentifier(table.getName());
    }

    protected <T> SelectStatementBuilder<T> appendConditions(SelectStatementBuilder<T> builder) {

        if (conditions != null && !conditions.isEmpty()) {

            String separator = " WHERE ";

            for (BinaryCondition c : conditions) {
                builder.append(separator);
                builder.appendIdentifier(c.getColumn())
                        .append(" ")
                        .append(c.getOperator().getSqlOperator())
                        .append(" ")
                        .appendBinding(table.getColumn(c.getColumn()), c.getValue());
                separator = " AND ";
            }
        }

        return builder;
    }

    public void assertIsPresent() {
        assertHasRows(1);
    }

    public void assertIsAbsent() {
        assertHasRows(0);
    }

    private Collection<BinaryCondition> getConditions() {

        if (conditions == null) {
            conditions = new ArrayList<>();
        }

        return conditions;
    }
}
