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

    public void assertMatches(int expectedRowCount) {

        SelectStatementBuilder<Integer> builder = countStatement();
        int count = appendConditions(builder)
                .select(1)
                .get(0);

        assertEquals("Unexpected row count in the DB", expectedRowCount, count);
    }

    protected SelectStatementBuilder<Integer> countStatement() {
        return table.selectStatement(RowReader.intReader())
                .append("SELECT COUNT(*) FROM ")
                .appendIdentifier(table.getName());
    }

    protected <T> SelectStatementBuilder<T> appendConditions(SelectStatementBuilder<T> builder) {

        if (conditions != null && !conditions.isEmpty()) {

            String separator = " WHERE ";

            for (BinaryCondition c : conditions) {

                // TODO: use some kind of tree visitor to parse conditions?

                // TODO: pull out SQL translation from here... things like NULL syntax make it non-trivial, so would
                //  be nice to reuse elsewhere

                builder.append(separator);

                if (c.getValue() != null) {

                    builder.appendIdentifier(c.getColumn())
                            .append(" ")
                            .append(c.getOperator().getSqlOperator())
                            .append(" ")
                            .appendBinding(table.getColumn(c.getColumn()), c.getValue());
                } else {
                    builder.appendIdentifier(c.getColumn()).append(" IS NULL");
                }

                separator = " AND ";
            }
        }

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
