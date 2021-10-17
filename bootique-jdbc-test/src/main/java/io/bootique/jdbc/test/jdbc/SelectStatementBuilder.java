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

package io.bootique.jdbc.test.jdbc;

import io.bootique.jdbc.test.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @param <T>
 * @deprecated since 3.0.M1, as we are we phasing out JUnit 4 support in favor of JUnit 5
 */
@Deprecated
public class SelectStatementBuilder<T> extends StatementBuilder<SelectStatementBuilder<T>> {

    private final RowReader rowReader;
    private final RowConverter<T> rowConverter;

    public SelectStatementBuilder(
            RowReader rowReader,
            RowConverter<T> rowConverter,
            DatabaseChannel channel,
            ObjectValueConverter objectValueConverter,
            BindingValueToStringConverter valueToStringConverter,
            IdentifierQuotationStrategy quotationStrategy) {
        super(channel, objectValueConverter, valueToStringConverter, quotationStrategy);
        this.rowReader = Objects.requireNonNull(rowReader);
        this.rowConverter = Objects.requireNonNull(rowConverter);
    }

    protected SelectStatementBuilder(
            RowReader rowReader,
            RowConverter<T> rowConverter,
            DatabaseChannel channel,
            ObjectValueConverter objectValueConverter,
            BindingValueToStringConverter valueToStringConverter,
            IdentifierQuotationStrategy quotationStrategy,
            List<Binding> bindings,
            StringBuilder sqlBuffer) {
        super(channel, objectValueConverter, valueToStringConverter, quotationStrategy, bindings, sqlBuffer);
        this.rowReader = Objects.requireNonNull(rowReader);
        this.rowConverter = Objects.requireNonNull(rowConverter);
    }

    /**
     * @since 2.0.B1
     */
    public SelectStatementBuilder<T> reader(RowReader reader) {
        return new SelectStatementBuilder<>(
                reader,
                this.rowConverter,
                this.channel,
                this.objectValueConverter,
                this.valueToStringConverter,
                this.quotationStrategy,
                this.bindings,
                this.sqlBuffer);
    }

    /**
     * @since 2.0.B1
     */
    public <U> SelectStatementBuilder<U> converter(RowConverter<U> converter) {
        return new SelectStatementBuilder<>(
                this.rowReader,
                converter,
                this.channel,
                this.objectValueConverter,
                this.valueToStringConverter,
                this.quotationStrategy,
                this.bindings,
                this.sqlBuffer);
    }

    /**
     * @since 2.0
     */
    public List<T> select() {
        return select(Long.MAX_VALUE);
    }

    public List<T> select(long maxRows) {

        String sql = getSql();
        log(sql, bindings);

        try {
            return selectWithExceptions(sql, maxRows);
        } catch (SQLException e) {
            throw new RuntimeException("Error running selecting SQL: " + sql, e);
        }
    }

    /**
     * @since 2.0.B1
     */
    public T selectOne() {
        return selectOne(null);
    }

    /**
     * @since 2.0.B1
     */
    public T selectOne(T defaultValue) {

        List<T> data = select(2);
        switch (data.size()) {
            case 0:
                return defaultValue;
            case 1:
                return data.get(0);
            default:
                throw new IllegalArgumentException("At most one row expected in the result");
        }
    }

    protected List<T> selectWithExceptions(String sql, long maxRows) throws SQLException {

        List<T> result = new ArrayList<>();

        try (Connection c = channel.getConnection()) {
            try (PreparedStatement st = c.prepareStatement(sql)) {

                bind(st);
                try (ResultSet rs = st.executeQuery()) {

                    while (rs.next() && result.size() < maxRows) {
                        result.add(rowConverter.convert(rowReader.readRow(rs)));
                    }
                }
            }
        }

        return result;
    }
}
