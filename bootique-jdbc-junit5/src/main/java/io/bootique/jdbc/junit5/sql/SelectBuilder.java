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

import io.bootique.jdbc.junit5.RowConverter;
import io.bootique.jdbc.junit5.RowReader;
import io.bootique.jdbc.junit5.metadata.DbColumnMetadata;

import java.util.List;

/**
 * @since 2.0.B1
 */
public class SelectBuilder<T> {

    protected final SelectStatementBuilder<T> builder;

    public SelectBuilder(SelectStatementBuilder<T> builder) {
        this.builder = builder;
    }

    /**
     * Returns another SelectBuilder that inherits the underlying SQL query, but uses a different reader for the result.
     */
    public SelectBuilder<T> reader(RowReader reader) {
        return new SelectBuilder<>(builder.reader(reader));
    }

    /**
     * Returns another SelectBuilder that inherits the underlying SQL query, but uses a converter for the result.
     */
    public <U> SelectBuilder<U> converter(RowConverter<U> converter) {
        return new SelectBuilder<>(builder.converter(converter));
    }

    /**
     * @since 3.0
     */
    public SelectWhereBuilder<T> where() {
        return new SelectWhereBuilder<>(builder);
    }

    public SelectWhereBuilder<T> where(String column, Object value) {
        return where(column, DbColumnMetadata.NO_TYPE, value);
    }

    /**
     * @since 3.0
     */
    public SelectWhereBuilder<T> where(String column, int valueType, Object value) {
        return new SelectWhereBuilder<>(builder).andEq(column, valueType, value);
    }

    /**
     * @deprecated since 3.0 in favor of {@link #where(String, int, Object)}
     */
    @Deprecated
    public SelectWhereBuilder<T> where(String column, Object value, int valueType) {
        return new SelectWhereBuilder<>(builder).andEq(column, valueType, value);
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
}
