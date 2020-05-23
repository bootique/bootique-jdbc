/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.jdbc.test.matcher;

import io.bootique.jdbc.test.Table;
import io.bootique.jdbc.test.RowReader;
import io.bootique.jdbc.test.connector.SelectStatementBuilder;
import io.bootique.jdbc.test.metadata.DbColumnMetadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @since 2.0
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

    /**
     * @since 1.1
     */
    public RowCountMatcher in(String column, Object... values) {
        getConditions().add(new BinaryCondition(column, BinaryCondition.Comparision.in, values));
        return this;
    }

    public void assertMatches(int expectedRowCount) {

        SelectStatementBuilder<Integer> builder = countStatement();
        int count = appendConditions(builder)
                .select(1)
                .get(0);

        assertEquals(expectedRowCount, count, "Unexpected row count in the DB");
    }

    protected SelectStatementBuilder<Integer> countStatement() {
        return table.selectStatement(RowReader.intReader())
                .append("select count(*) from ")
                .appendTableName(table.getMetadata().getName());
    }

    protected <T> SelectStatementBuilder<T> appendConditions(SelectStatementBuilder<T> builder) {

        if (conditions != null && !conditions.isEmpty()) {

            String separator = " where ";

            for (BinaryCondition c : conditions) {

                // TODO: use some kind of tree visitor to parse conditions?

                // TODO: pull out SQL translation from here... things like NULL syntax make it non-trivial, so would
                //  be nice to reuse elsewhere

                builder.append(separator);

                switch (c.getOperator()) {
                    case eq:
                        appendEq(builder, c);
                        break;
                    case in:
                        appendIn(builder, c);
                        break;
                    default:
                        throw new UnsupportedOperationException("Unexpected operator: " + c.getOperator());
                }

                separator = " and ";
            }
        }

        return builder;
    }

    protected <T> SelectStatementBuilder<T> appendEq(SelectStatementBuilder<T> builder, BinaryCondition eq) {
        if (eq.getValue() != null) {

            builder.appendIdentifier(eq.getColumn())
                    .append(" ")
                    .append("=")
                    .append(" ")
                    .appendBinding(table.getMetadata().getColumn(eq.getColumn()), eq.getValue());
        } else {
            builder.appendIdentifier(eq.getColumn()).append(" IS NULL");
        }

        return builder;
    }

    protected <T> SelectStatementBuilder<T> appendIn(SelectStatementBuilder<T> builder, BinaryCondition in) {

        if (in.getValue() instanceof Object[]) {
            return appendIn(builder, in.getColumn(), (Object[]) in.getValue());
        } else if (in.getValue() instanceof Collection) {
            Collection<?> c = (Collection<?>) in.getValue();
            Object[] array = new Object[c.size()];
            c.toArray(array);
            return appendIn(builder, in.getColumn(), array);
        } else {
            // null collection or scalar value
            return appendEq(builder, in);
        }
    }

    protected <T> SelectStatementBuilder<T> appendIn(SelectStatementBuilder<T> builder, String columnName, Object[] values) {

        builder.appendIdentifier(columnName).append(" in (");

        DbColumnMetadata column = table.getMetadata().getColumn(columnName);

        // TODO: how to handle empty collections?
        for (int i = 0; i < values.length; i++) {

            // TODO: NULL support via an extra "OR c IS NULL" clause
            if (values[i] == null) {
                throw new IllegalArgumentException("Can't use nulls in the 'IN' condition");
            }

            if (i > 0) {
                builder.append(",");
            }
            builder.appendBinding(column, values[i]);
        }

        builder.append(")");

        return builder;
    }

    public void assertOneMatch() {
        assertMatches(1);
    }

    public void assertNoMatches() {
        assertMatches(0);
    }

    private Collection<BinaryCondition> getConditions() {

        if (conditions == null) {
            conditions = new ArrayList<>();
        }

        return conditions;
    }
}
