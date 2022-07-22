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
package io.bootique.jdbc.junit5.connector;

import io.bootique.jdbc.junit5.metadata.DbColumnMetadata;

/**
 * @since 3.0
 */
public abstract class WhereBuilder<
        WHERE_BUILDER extends WhereBuilder,
        STATEMENT_BUILDER extends StatementBuilder<STATEMENT_BUILDER>> {

    protected final STATEMENT_BUILDER builder;
    protected int whereCount;

    protected WhereBuilder(STATEMENT_BUILDER builder) {
        this.builder = builder;
    }

    public WHERE_BUILDER and(String column, Object value) {
        return and(column, value, DbColumnMetadata.NO_TYPE);
    }

    public WHERE_BUILDER and(String column, Object value, int valueType) {

        if (whereCount++ > 0) {
            builder.append(" and ");
        } else {
            builder.append(" where ");
        }

        builder.appendIdentifier(column)
                .append(" = ")
                .appendBinding(column, valueType, value);

        return (WHERE_BUILDER) this;
    }

    public WHERE_BUILDER or(String column, Object value) {
        return or(column, value, DbColumnMetadata.NO_TYPE);
    }

    public WHERE_BUILDER or(String column, Object value, int valueType) {
        if (whereCount++ > 0) {
            builder.append(" or ");
        }

        builder.appendIdentifier(column)
                .append(" = ")
                .appendBinding(column, valueType, value);

        return (WHERE_BUILDER) this;
    }
}
