package io.bootique.jdbc.test.matcher;

import io.bootique.jdbc.test.Table;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @since 0.24
 */
public class RowCountMatcher {

    private Table table;
    private Collection<BinaryCondition> conditions;

    public RowCountMatcher(Table table) {
        this.table = table;
    }

    public RowCountMatcher eq(String column, Object value) {
        getConditions().add(new BinaryCondition(column, BinaryCondition.Comparision.eq, value));
        return this;
    }

    public void assertHasRows(int expectedRowCount) {
        throw new UnsupportedOperationException("TODO");
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
