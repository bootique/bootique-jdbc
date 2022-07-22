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

package io.bootique.jdbc.junit5.matcher;

import io.bootique.jdbc.junit5.Table;
import io.bootique.jdbc.junit5.sql.SelectBuilder;
import io.bootique.jdbc.junit5.sql.SelectStatementBuilder;
import io.bootique.jdbc.junit5.sql.SelectWhereBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @since 2.0
 */
public class RowCountMatcher {

    private final Table table;
    private final SelectWhereBuilder<Integer> countBuilder;

    public RowCountMatcher(Table table) {
        this.table = table;
        this.countBuilder = countBuilder(table).where();
    }

    /**
     * @since 3.0
     */
    public RowCountMatcher andEq(String column, Object value) {
        countBuilder.andEq(column, table.getMetadata().getColumn(column).getType(), value);
        return this;
    }

    /**
     * @deprecated since 3.0 in favor of {@link #andEq(String, Object)}
     */
    @Deprecated
    public RowCountMatcher eq(String column, Object value) {
        return andEq(column, value);
    }

    /**
     * @since 3.0
     */
    public RowCountMatcher andIn(String column, Object... values) {
        countBuilder.andIn(column, table.getMetadata().getColumn(column).getType(), values);
        return this;
    }

    /**
     * @deprecated since 3.0 in favor of {@link #andIn(String, Object...)}
     */
    @Deprecated
    public RowCountMatcher in(String column, Object... values) {
        return andIn(column, values);
    }

    public void assertMatches(int expectedRowCount) {
        int count = countBuilder.select(1).get(0);
        assertEquals(expectedRowCount, count, "Unexpected row count in the DB");
    }

    protected SelectBuilder<Integer> countBuilder(Table table) {
        SelectStatementBuilder<Integer> statementBuilder = table.getConnector()
                .selectStatement()
                // TODO: count() would usually return Long. We don't expect such large numbers in tests,
                //  but wonder if we should still change the signature to return Long for consistency?
                .converter(r -> ((Number) r[0]).intValue())
                .append("select count(*) from ")
                .appendTableName(table.getMetadata().getName());
        return new SelectBuilder<>(statementBuilder);
    }

    public void assertOneMatch() {
        assertMatches(1);
    }

    public void assertNoMatches() {
        assertMatches(0);
    }
}
