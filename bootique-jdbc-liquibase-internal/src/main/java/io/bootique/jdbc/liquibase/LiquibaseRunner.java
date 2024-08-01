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

import io.bootique.resource.ResourceFactory;
import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.Liquibase;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @since 2.0
 */
public class LiquibaseRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseRunner.class);

    protected Collection<ResourceFactory> changeLogs;
    protected DataSource dataSource;
    protected String defaultSchema;

    public LiquibaseRunner(Collection<ResourceFactory> changeLogs, DataSource dataSource, String defaultSchema) {
        this.changeLogs = Objects.requireNonNull(changeLogs);
        this.dataSource = Objects.requireNonNull(dataSource);
        this.defaultSchema = defaultSchema;
    }

    public <T> T call(Function<Liquibase, T> op) {

        Liquibase lb = createLiquibase();
        try {
            return op.apply(lb);
        } finally {
            closeLiquibase(lb);
        }
    }

    public void run(Consumer<Liquibase> op) {

        Liquibase lb = createLiquibase();
        try {
            op.accept(lb);
        } finally {
            closeLiquibase(lb);
        }
    }

    protected Liquibase createLiquibase() {
        ResourceAccessor resourceAccessor = new ResourceFactoryAccessor();

        try {
            Database liquibaseDB = createDatabase(dataSource.getConnection());
            DatabaseChangeLog changeLog = createDatabaseChangeLog(liquibaseDB, resourceAccessor);
            return new Liquibase(changeLog, resourceAccessor, liquibaseDB);
        } catch (SQLException | LiquibaseException e) {
            throw new RuntimeException("Error creating liquibase", e);
        }
    }

    protected DatabaseChangeLog createDatabaseChangeLog(Database database, ResourceAccessor resourceAccessor) {
        DatabaseChangeLog changeLog = new DatabaseChangeLog();

        // adding a star
        changeLog.setLogicalFilePath("bootique-liquibase-root*");
        changeLog.setChangeLogParameters(new ChangeLogParameters(database));

        // TODO: do something useful with this?
        Labels labels = new Labels();

        changeLogs.forEach(cl -> {
            try {
                LOGGER.info("Including change log: '{}'", cl.getResourceId());
                changeLog.include(cl.getResourceId(), false, true, resourceAccessor, new ContextExpression(), labels, false, DatabaseChangeLog.OnUnknownFileFormat.FAIL);
            } catch (LiquibaseException e) {
                throw new RuntimeException("Error configuring Liquibase", e);
            }
        });

        return changeLog;
    }

    protected Database createDatabase(Connection c) throws DatabaseException {

        DatabaseConnection liquibaseConnection = new JdbcConnection(c);
        DatabaseFactory databaseFactory = DatabaseFactory.getInstance();

        // Custom Derby DB to prevent Derby shutdown by Liquibase. Liquibase operates with Bootique-provided DataSource
        // and should not attempt to manage the underlying DB state.
        databaseFactory.register(new NonClosingDerbyDatabase());

        Database database = databaseFactory.findCorrectDatabaseImplementation(liquibaseConnection);

        if (defaultSchema != null) {
            database.setDefaultSchemaName(defaultSchema);
        }

        return database;
    }

    protected void closeLiquibase(Liquibase lb) {
        if (lb.getDatabase() != null) {
            try {
                lb.getDatabase().close();
            } catch (DatabaseException e) {
                LOGGER.info("Error closing Liquibase, ignored", e);
            }
        }
    }

}
