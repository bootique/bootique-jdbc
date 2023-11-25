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

package io.bootique.jdbc.test;

import io.bootique.jdbc.test.jdbc.SelectStatementBuilder;

import java.util.List;

/**
 * @since 2.0
 * @deprecated since 3.0.M1, as we are we phasing out JUnit 4 support in favor of JUnit 5
 */
@Deprecated
public class SelectWhereBuilder<T> {

    protected final SelectStatementBuilder<T> builder;
    protected int whereCount;

    public SelectWhereBuilder(SelectStatementBuilder<T> builder) {
        this.builder = builder;
    }

    public List<T> select() {
        return builder.select();
    }

    public List<T> select(long maxRows) {
        return builder.select(maxRows);
    }

    public T selectOne() {
        return builder.selectOne(null);
    }

    public T selectOne(T defaultValue) {
        return builder.selectOne(defaultValue);
    }

    public SelectWhereBuilder<T> and(String column, Object value) {
        return and(column, value, Column.NO_TYPE);
    }

    public SelectWhereBuilder<T> and(String column, Object value, int valueType) {

        if (whereCount++ > 0) {
            builder.append(" and ");
        } else {
            builder.append(" where ");
        }

        builder.appendIdentifier(column)
                .append(" = ")
                .appendBinding(column, valueType, value);

        return this;
    }

    public SelectWhereBuilder<T> or(String column, Object value) {
        return or(column, value, Column.NO_TYPE);
    }

    public SelectWhereBuilder<T> or(String column, Object value, int valueType) {
        if (whereCount++ > 0) {
            builder.append(" or ");
        }

        builder.appendIdentifier(column)
                .append(" = ")
                .appendBinding(column, valueType, value);

        return this;
    }
}
