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

package io.bootique.jdbc.test.junit5;

import io.bootique.jdbc.test.Table;
import io.bootique.jdbc.test.matcher.TableMatcher;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.HashMap;
import java.util.Map;

/**
 * A unit test "rule" to manage data sets for unit tests.
 *
 * @since 2.0
 */
public class TestDataManager implements BeforeEachCallback {

    private Table[] tablesInInsertOrder;
    private Map<String, Table> tables;
    private boolean deleteData;

    public TestDataManager(boolean deleteData, Table... tablesInInsertOrder) {
        this.deleteData = deleteData;
        this.tablesInInsertOrder = tablesInInsertOrder != null ? tablesInInsertOrder : new Table[0];

        tables = new HashMap<>();
        for (Table t : this.tablesInInsertOrder) {
            tables.put(t.getName(), t);
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        // do cleanup before the test; leave the data alone *after* - perhaps we want to inspect it manually, etc.
        if (deleteData) {
            deleteData();
        }
    }

    /**
     * Deletes data from the managed tables.
     *
     * @since 0.24
     */
    public void deleteData() {
        for (int i = tablesInInsertOrder.length - 1; i >= 0; i--) {
            tablesInInsertOrder[i].deleteAll();
        }
    }

    /**
     * @param tableName the name of a table whose data we'd like to check.
     * @return a new TableMatcher for the specified table.
     * @since 0.24
     */
    public TableMatcher matcher(String tableName) {
        return getTable(tableName).matcher();
    }

    public Table getTable(String tableName) {
        return tables.computeIfAbsent(tableName, n -> {
            throw new IllegalArgumentException("Unknown table name: " + n);
        });
    }

    /**
     * @return an array of registered tables in insert order.
     * @since 0.24
     */
    public Table[] getTablesInInsertOrder() {
        return tablesInInsertOrder;
    }
}
