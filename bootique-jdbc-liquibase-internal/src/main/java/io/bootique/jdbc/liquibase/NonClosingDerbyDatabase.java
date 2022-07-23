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

package io.bootique.jdbc.liquibase;

import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.DerbyDatabase;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;

import java.lang.reflect.Field;

/**
 * Reimplements "close" method for Liquibase {@link liquibase.database.core.DerbyDatabase} to prevent Derby shutdown.
 * Liquibase operates with Bootique-provided DataSource and should not attempt to manage the underlying DB state.
 *
 * @since 2.0
 */
public class NonClosingDerbyDatabase extends DerbyDatabase {

    @Override
    public int getPriority() {
        // higher priority allows us to override default Derby DB object
        return super.getPriority() + 1;
    }

    @Override
    public void close() throws DatabaseException {

        // copy-paste from AbstractJdbcDatabase plus reflection to deal with private fields...

        Scope.getCurrentScope().getSingleton(ExecutorService.class).clearExecutor("jdbc", this);
        DatabaseConnection connection = getConnection();
        if (connection != null) {

            Boolean previousAutoCommit = previousAutoCommit();
            if (previousAutoCommit != null) {
                try {
                    connection.setAutoCommit(previousAutoCommit);
                } catch (DatabaseException e) {
                    Scope.getCurrentScope().getLog(getClass()).warning("Failed to restore the auto commit to " + previousAutoCommit);

                    throw e;
                }
            }
            connection.close();
        }
    }

    private Boolean previousAutoCommit() throws DatabaseException {

        // ugly hack ... reading private field from superclass...

        Field fPreviousAutoCommit;
        try {
            fPreviousAutoCommit = AbstractJdbcDatabase.class.getDeclaredField("previousAutoCommit");
        } catch (NoSuchFieldException e) {
            throw new DatabaseException("Error reading auto-commit data", e);
        }

        fPreviousAutoCommit.setAccessible(true);
        try {
            return (Boolean) fPreviousAutoCommit.get(this);
        } catch (IllegalAccessException e) {
            throw new DatabaseException("Error reading auto-commit data", e);
        }
    }
}
