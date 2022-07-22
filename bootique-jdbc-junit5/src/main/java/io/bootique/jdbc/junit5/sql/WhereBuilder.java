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

import java.util.Collection;

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

    /**
     * @since 3.0
     */
    public WHERE_BUILDER andOp(String column, ComparisonOp op, Object value) {
        return andOp(column, op, DbColumnMetadata.NO_TYPE, value);
    }

    /**
     * @since 3.0
     */
    public WHERE_BUILDER andOp(String column, ComparisonOp op, int valueType, Object value) {
        return appendCondition("and", column, op, valueType, value);
    }

    /**
     * @since 3.0
     */
    public WHERE_BUILDER orOp(String column, ComparisonOp op, Object value) {
        return orOp(column, op, DbColumnMetadata.NO_TYPE, value);
    }

    /**
     * @since 3.0
     */
    public WHERE_BUILDER orOp(String column, ComparisonOp op, int valueType, Object value) {
        return appendCondition("or", column, op, valueType, value);
    }

    /**
     * @since 3.0
     */
    public WHERE_BUILDER andEq(String column, Object value) {
        return andEq(column, DbColumnMetadata.NO_TYPE, value);
    }

    /**
     * @since 3.0
     */
    public WHERE_BUILDER andEq(String column, int valueType, Object value) {
        return andOp(column, ComparisonOp.eq, valueType, value);
    }

    /**
     * @since 3.0
     */
    public WHERE_BUILDER orEq(String column, Object value) {
        return orEq(column, DbColumnMetadata.NO_TYPE, value);
    }

    /**
     * @since 3.0
     */
    public WHERE_BUILDER orEq(String column, int valueType, Object value) {
        return orOp(column, ComparisonOp.eq, valueType, value);
    }

    /**
     * @since 3.0
     */
    public WHERE_BUILDER andIn(String column, Object... values) {
        return andIn(column, DbColumnMetadata.NO_TYPE, values);
    }

    /**
     * @since 3.0
     */
    public WHERE_BUILDER andIn(String column, int valueType, Object... values) {
        return appendCondition("and", column, ComparisonOp.in, valueType, values);
    }

    /**
     * @since 3.0
     */
    public WHERE_BUILDER orIn(String column, Object... values) {
        return orIn(column, DbColumnMetadata.NO_TYPE, values);
    }

    /**
     * @since 3.0
     */
    public WHERE_BUILDER orIn(String column, int valueType, Object... values) {
        return appendCondition("or", column, ComparisonOp.in, valueType, values);
    }

    /**
     * @deprecated since 3.0 in favor of {@link #andEq(String, Object)}
     */
    @Deprecated
    public WHERE_BUILDER and(String column, Object value) {
        return andEq(column, value);
    }

    /**
     * @deprecated since 3.0 in favor of {@link #andEq(String, int, Object)}
     */
    @Deprecated
    public WHERE_BUILDER and(String column, Object value, int valueType) {
        return andEq(column, valueType, value);
    }

    /**
     * @deprecated since 3.0 in favor of {@link #orEq(String, Object)}
     */
    @Deprecated
    public WHERE_BUILDER or(String column, Object value) {
        return orEq(column, value);
    }

    /**
     * @deprecated since 3.0 in favor of {@link #orEq(String, int, Object)}
     */
    @Deprecated
    public WHERE_BUILDER or(String column, Object value, int valueType) {
        return orEq(column, valueType, value);
    }

    protected WHERE_BUILDER appendCondition(String joinWith, String column, ComparisonOp op, int valueType, Object value) {
        if (whereCount++ == 0) {
            builder.append(" where ");
        } else {
            builder.append(" ").append(joinWith).append(" ");
        }

        switch (op) {
            case eq:
                return appendEq(column, valueType, value);
            case ne:
                return appendNe(column, valueType, value);
            case in:
                return appendIn(column, valueType, value);
            case ge:
                return appendKV(column, ">=", valueType, value);
            case gt:
                return appendKV(column, ">", valueType, value);
            case le:
                return appendKV(column, "<=", valueType, value);
            case lt:
                return appendKV(column, "<", valueType, value);
            default:
                throw new IllegalArgumentException("Unknown SQL comparsion operator: " + op);
        }
    }

    protected WHERE_BUILDER appendEq(String column, int valueType, Object value) {

        if (value != null) {
            builder.appendIdentifier(column)
                    .append(" = ")
                    .appendBinding(column, valueType, value);
        } else {
            builder.appendIdentifier(column).append(" IS NULL");
        }

        return (WHERE_BUILDER) this;
    }

    protected WHERE_BUILDER appendNe(String column, int valueType, Object value) {
        if (value != null) {
            builder.appendIdentifier(column)
                    .append(" <> ")
                    .appendBinding(column, valueType, value);
        } else {
            builder.appendIdentifier(column).append(" IS NOT NULL");
        }

        return (WHERE_BUILDER) this;
    }

    protected WHERE_BUILDER appendKV(String column, String op, int valueType, Object value) {

        if (value == null) {
            return appendFalse();
        }

        builder.appendIdentifier(column)
                .append(" ")
                .append(op)
                .append(" ")
                .appendBinding(column, valueType, value);

        return (WHERE_BUILDER) this;
    }

    protected WHERE_BUILDER appendIn(String column, int valueType, Object value) {
        if (value instanceof Object[]) {
            return appendInArray(column, valueType, (Object[]) value);
        } else if (value instanceof Collection) {
            Collection<?> c = (Collection<?>) value;
            return appendInArray(column, valueType, c.toArray(new Object[0]));
        } else {
            return appendEq(column, DbColumnMetadata.NO_TYPE, value);
        }
    }

    protected WHERE_BUILDER appendInArray(String column, int valueType, Object[] values) {

        if (values.length == 0) {
            return appendFalse();
        }

        if (values.length == 1) {
            return appendEq(column, DbColumnMetadata.NO_TYPE, values[0]);
        }

        builder.appendIdentifier(column).append(" in (");

        // TODO: how to handle empty collections?
        for (int i = 0; i < values.length; i++) {

            // TODO: NULL support via an extra "OR c IS NULL" clause
            if (values[i] == null) {
                throw new IllegalArgumentException("Can't use nulls in the 'IN' condition");
            }

            if (i > 0) {
                builder.append(",");
            }
            builder.appendBinding(column, valueType, values[i]);
        }

        builder.append(")");
        return (WHERE_BUILDER) this;
    }

    protected WHERE_BUILDER appendFalse() {
        builder.append("1 = 2");
        return (WHERE_BUILDER) this;
    }
}
