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
import java.sql.SQLException;

/**
 * @since 0.24
 */
public class ExecStatementBuilder extends StatementBuilder<ExecStatementBuilder> {

    public ExecStatementBuilder(
            DatabaseChannel channel,
            ObjectValueConverter objectValueConverter,
            BindingValueToStringConverter valueToStringConverter,
            IdentifierQuotationStrategy quotationStrategy) {
        super(channel, objectValueConverter, valueToStringConverter, quotationStrategy);
    }

    public int exec(String sql) {
        return append(sql).exec();
    }

    public int exec() {

        String sql = getSql();
        log(sql, bindings);

        try {
            return execWithExceptions(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error running updating SQL: " + sql, e);
        }
    }

    protected int execWithExceptions(String sql) throws SQLException {

        try (Connection c = channel.getConnection();) {

            int count;
            try (PreparedStatement st = c.prepareStatement(sql)) {
                bind(st);
                count = st.executeUpdate();
            }

            c.commit();
            return count;
        }
    }
}
