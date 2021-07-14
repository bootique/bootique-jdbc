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

import io.bootique.jdbc.test.jdbc.ExecStatementBuilder;

public class UpdateSetBuilder {

    protected ExecStatementBuilder builder;
    protected int setCount;

    protected UpdateSetBuilder(ExecStatementBuilder builder) {
        this.builder = builder;
    }

    /**
     * @return the number of updated records.
     */
    public int exec() {
        return builder.exec();
    }


    public UpdateSetBuilder set(String column, Object value) {
        return set(column, value, Column.NO_TYPE);
    }

    public UpdateSetBuilder set(String column, Object value, int valueType) {
        if (setCount++ > 0) {
            builder.append(", ");
        }

        builder.appendIdentifier(column)
                .append(" = ")
                .appendBinding(column, valueType, value);
        return this;
    }

    public ExecWhereBuilder where(String column, Object value) {
        return where(column, value, Column.NO_TYPE);
    }

    public ExecWhereBuilder where(String column, Object value, int valueType) {
        ExecWhereBuilder where = new ExecWhereBuilder(builder);
        where.and(column, value, valueType);
        return where;
    }
}
