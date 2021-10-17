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

import io.bootique.BQRuntime;
import io.bootique.jdbc.test.jdbc.ExecStatementBuilder;
import io.bootique.jdbc.test.jdbc.SelectStatementBuilder;
import io.bootique.jdbc.test.runtime.DatabaseChannelFactory;

import java.sql.Connection;

/**
 * @deprecated since 3.0.M1, as we are we phasing out JUnit 4 support in favor of JUnit 5
 */
@Deprecated
public interface DatabaseChannel {

    static DatabaseChannel get(BQRuntime runtime) {
        return runtime.getInstance(DatabaseChannelFactory.class).getChannel();
    }

    static DatabaseChannel get(BQRuntime runtime, String dataSourceName) {
        return runtime.getInstance(DatabaseChannelFactory.class).getChannel(dataSourceName);
    }

    default Table.Builder newTable(String tableName) {
        return Table.builder(this, tableName);
    }

    /**
     * @return DB-specific identifier quotation symbol.
     */
    String getIdentifierQuote();

    Connection getConnection();

    void close();

    /**
     * @return a new {@link ExecStatementBuilder} object that assists in creating and executing a PreparedStatement.
     */
    ExecStatementBuilder execStatement();

    /**
     * @return a new {@link SelectStatementBuilder} object that assists in creating and running a selecting
     * PreparedStatement.
     */
    SelectStatementBuilder<Object[]> selectStatement();
}
