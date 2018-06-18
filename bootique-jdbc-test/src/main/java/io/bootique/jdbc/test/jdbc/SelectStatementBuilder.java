/**
 *  Licensed to ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.jdbc.test.jdbc;

import io.bootique.jdbc.test.BindingValueToStringConverter;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.IdentifierQuotationStrategy;
import io.bootique.jdbc.test.ObjectValueConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.24
 */
public class SelectStatementBuilder<T> extends StatementBuilder<SelectStatementBuilder<T>> {

    private RowReader<T> rowReader;

    public SelectStatementBuilder(
            RowReader<T> rowReader,
            DatabaseChannel channel,
            ObjectValueConverter objectValueConverter,
            BindingValueToStringConverter valueToStringConverter,
            IdentifierQuotationStrategy quotationStrategy) {
        super(channel, objectValueConverter, valueToStringConverter, quotationStrategy);
        this.rowReader = rowReader;
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

    protected List<T> selectWithExceptions(String sql, long maxRows) throws SQLException {

        List<T> result = new ArrayList<>();

        try (Connection c = channel.getConnection()) {
            try (PreparedStatement st = c.prepareStatement(sql)) {

                bind(st);
                try (ResultSet rs = st.executeQuery()) {

                    while (rs.next() && result.size() < maxRows) {
                        result.add(rowReader.readRow(rs));
                    }
                }
            }
        }

        return result;
    }
}
