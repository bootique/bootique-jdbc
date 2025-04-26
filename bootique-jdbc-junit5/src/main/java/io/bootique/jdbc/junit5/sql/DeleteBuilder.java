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
package io.bootique.jdbc.junit5.sql;

import io.bootique.jdbc.junit5.metadata.DbColumnMetadata;

/**
 * @since 2.0
 */
public class DeleteBuilder {

    protected final ExecStatementBuilder builder;

    public DeleteBuilder(ExecStatementBuilder builder) {
        this.builder = builder;
    }

    /**
     * @since 3.0
     */
    public ExecWhereBuilder where() {
        return new ExecWhereBuilder(builder);
    }

    public ExecWhereBuilder where(String column, Object value) {
        return where(column, DbColumnMetadata.NO_TYPE, value);
    }

    /**
     * @since 3.0
     */
    public ExecWhereBuilder where(String column, int valueType, Object value) {
        return new ExecWhereBuilder(builder).andEq(column, valueType, value);
    }

    public int exec() {
        return builder.exec();
    }
}
