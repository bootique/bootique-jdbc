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
package io.bootique.jdbc.test.tester;

import io.bootique.jdbc.test.connector.DbConnector;

/**
 * Provides test data manager facilities to the {@link io.bootique.jdbc.test.JdbcTester}.
 */
public class DataManager {

    private DbConnector connector;
    private String[] tablesInInsertOrder;

    public DataManager(DbConnector connector, String[] tablesInInsertOrder) {
        this.connector = connector;
        this.tablesInInsertOrder = tablesInInsertOrder;
    }

    public void deleteData() {
        for (int i = tablesInInsertOrder.length - 1; i >= 0; i--) {
            connector.getTable(tablesInInsertOrder[i]).deleteAll();
        }
    }
}
