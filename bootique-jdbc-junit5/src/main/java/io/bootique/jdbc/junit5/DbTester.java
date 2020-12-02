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
package io.bootique.jdbc.junit5;

import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Key;
import io.bootique.jdbc.junit5.connector.DbConnector;
import io.bootique.jdbc.junit5.connector.ExecStatementBuilder;
import io.bootique.jdbc.junit5.datasource.DataSourceHolder;
import io.bootique.jdbc.junit5.datasource.PoolingDataSource;
import io.bootique.jdbc.junit5.datasource.PoolingDataSourceParameters;
import io.bootique.jdbc.junit5.metadata.DbMetadata;
import io.bootique.jdbc.junit5.tester.DataManager;
import io.bootique.jdbc.junit5.tester.DataSourcePropertyBuilder;
import io.bootique.jdbc.junit5.tester.SqlScriptParser;
import io.bootique.jdbc.liquibase.LiquibaseRunner;
import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.scope.BQAfterScopeCallback;
import io.bootique.junit5.scope.BQBeforeMethodCallback;
import io.bootique.junit5.scope.BQBeforeScopeCallback;
import io.bootique.resource.ResourceFactory;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Objects;

/**
 * A JUnit 5 extension that manages a single test database. DbTester is declared in a unit test and handles database
 * startup, schema and data initialization and shutdown. A single database controlled by DbTester can be used by
 * one or more BQRuntimes. This class is abstract. Specific testers (such as DerbyTester or TestcontainersTester)
 * are provided in separate modules.
 *
 * @since 2.0
 */
public abstract class DbTester<SELF extends DbTester> implements BQBeforeScopeCallback, BQAfterScopeCallback, BQBeforeMethodCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbTester.class);

    protected ResourceFactory initDBScript;
    protected String initDBScriptDelimiter;
    protected JdbcOp initFunction;
    protected ResourceFactory liquibaseChangeLog;
    protected String liquibaseContext;
    protected String[] deleteTablesInInsertOrder;

    protected final DataSourceHolder dataSourceHolder;
    protected DbConnector connector;

    public DbTester() {
        this.dataSourceHolder = new DataSourceHolder();
    }

    public DataSource getDataSource() {
        return dataSourceHolder;
    }

    protected DbConnector getConnector() {
        return Objects.requireNonNull(connector, "'connector' not initialized. Called outside of JUnit lifecycle?");
    }

    public DbMetadata getMetadata() {
        return getConnector().getMetadata();
    }

    public Table getTable(String name) {
        return getConnector().getTable(name);
    }

    public ExecStatementBuilder execStatement() {
        return getConnector().execStatement();
    }

    public Connection getConnection() {
        return getConnector().getConnection();
    }

    protected void configure(Binder binder, String dataSourceName) {
        bindSelf(binder, dataSourceName);
        configureDataSource(binder, dataSourceName);
    }

    protected void bindSelf(Binder binder, String dataSourceName) {
        binder.bind(Key.get(DbTester.class, dataSourceName)).toInstance(this);
    }

    protected void configureDataSource(Binder binder, String dataSourceName) {
        DataSourcePropertyBuilder.create(binder, dataSourceName).property("type", "bqjdbctest");
    }

    /**
     * Executes a provided SQL script after the DB startup. The script would usually contain database schema and test
     * data. Assumes statements are separated with ";" character.
     *
     * @param initDBScript a location of the SQL script in Bootique {@link io.bootique.resource.ResourceFactory} format.
     * @return this tester
     */
    public SELF initDB(String initDBScript) {
        return initDB(initDBScript, null);
    }

    /**
     * Executes a provided SQL script after the DB startup. The script would usually contain database schema and test
     * data.
     *
     * @param initDBScript a location of the SQL script in Bootique {@link io.bootique.resource.ResourceFactory} format.
     * @param delimiter    SQL statements delimiter in the "initDBScript". An explicit delimiter may be useful when
     *                     the file contains common DB delimiters in the middle of stored procedure declarations, etc.
     * @return this tester
     */
    public SELF initDB(String initDBScript, String delimiter) {
        this.initDBScript = new ResourceFactory(initDBScript);
        this.initDBScriptDelimiter = delimiter;
        return (SELF) this;
    }

    public SELF initDB(JdbcOp initFunction) {
        this.initFunction = Objects.requireNonNull(initFunction);
        return (SELF) this;
    }

    /**
     * Executes provides Liquibase changelog file after the DB startup.
     *
     * @param liquibaseChangeLog a location of the Liquibase changelog file in Bootique
     *                           {@link io.bootique.resource.ResourceFactory} format.
     * @return this tester
     */
    public SELF runLiquibaseMigrations(String liquibaseChangeLog) {
        this.liquibaseChangeLog = new ResourceFactory(liquibaseChangeLog);
        return (SELF) this;
    }

    /**
     * Executes provides Liquibase changelog file after the DB startup.
     *
     * @param liquibaseChangeLog a location of the Liquibase changelog file in Bootique
     *                           {@link io.bootique.resource.ResourceFactory} format.
     * @param liquibaseContext   Liquibase context expression to filter migrations as appropriate for the test run.
     * @return this tester
     */
    public SELF runLiquibaseMigrations(String liquibaseChangeLog, String liquibaseContext) {
        this.liquibaseChangeLog = new ResourceFactory(liquibaseChangeLog);
        this.liquibaseContext = liquibaseContext;
        return (SELF) this;
    }

    /**
     * Configures the Tester to delete data from the specified tables before each test.
     *
     * @param tablesInInsertOrder a list of table names in the order of INSERT dependencies between them.
     * @return this tester
     */
    public SELF deleteBeforeEachTest(String... tablesInInsertOrder) {
        this.deleteTablesInInsertOrder = tablesInInsertOrder;
        return (SELF) this;
    }

    /**
     * Returns a Bootique module that can be used to configure a test DataSource in test {@link io.bootique.BQRuntime}.
     * This method can be used to initialize one or more BQRuntimes in a test class, so that they can share the database
     * managed by this tester.
     *
     * @param dataSourceName the name of the DataSource to create or replace in the target runtime
     * @return a new Bootique module with test DataSource configuration.
     */
    public BQModule moduleWithTestDataSource(String dataSourceName) {
        return binder -> configure(binder, dataSourceName);
    }

    protected PoolingDataSource createPoolingDataSource(BQTestScope scope) {
        PoolingDataSourceParameters parameters = new PoolingDataSourceParameters();
        parameters.setMaxConnections(5);
        parameters.setMinConnections(1);
        parameters.setMaxQueueWaitTime(20000);

        return new PoolingDataSource(createNonPoolingDataSource(scope), parameters);
    }

    protected abstract DataSource createNonPoolingDataSource(BQTestScope scope);

    protected void initConnector() {
        this.connector = new DbConnector(dataSourceHolder, DbMetadata.create(dataSourceHolder));
    }

    protected void execInitFunction() {
        if (initFunction != null) {

            LOGGER.info("initializing DB from using custom init function");

            try (Connection c = dataSourceHolder.getConnection()) {
                initFunction.run(c);
            } catch (SQLException e) {
                throw new RuntimeException("Error running custom init function: " + e.getMessage(), e);
            }
        }
    }

    protected void execInitScript() {
        if (initDBScript != null) {

            LOGGER.info("initializing DB from {}", initDBScript.getUrl());
            String delimiter = this.initDBScriptDelimiter != null ? this.initDBScriptDelimiter : ";";
            Iterable<String> statements = new SqlScriptParser("--", "/*", "*/", delimiter).getStatements(initDBScript);

            try (Connection c = dataSourceHolder.getConnection()) {

                for (String sql : statements) {
                    try (PreparedStatement statement = c.prepareStatement(sql)) {
                        statement.execute();
                    } catch (SQLException e) {
                        throw new RuntimeException("Error running SQL statement " + sql + ": " + e.getMessage(), e);
                    }
                }

            } catch (SQLException e) {
                throw new RuntimeException("Error running SQL from " + initDBScript.getUrl() + ": " + e.getMessage(), e);
            }
        }
    }

    protected void execLiquibaseMigrations() {
        if (liquibaseChangeLog != null) {
            LOGGER.info("executing Liquibase migrations from {}", liquibaseChangeLog.getUrl());
            new LiquibaseRunner(Collections.singletonList(liquibaseChangeLog), dataSourceHolder, null)
                    .run(this::execLiquibaseMigrations);
        }
    }

    protected void execLiquibaseMigrations(Liquibase lb) {
        Contexts contexts = liquibaseContext != null ? new Contexts(liquibaseContext) : new Contexts();

        try {
            lb.update(contexts, new LabelExpression());
        } catch (Exception e) {
            throw new RuntimeException("Error running migrations against the test DB", e);
        }
    }

    @Override
    public void beforeScope(BQTestScope scope, ExtensionContext context) {

        // By now the DataSource may already be initialized
        // if BQRuntime using DbTester had some eager dependencies on DataSource

        dataSourceHolder.initIfNeeded(() -> createPoolingDataSource(scope), this::afterDataSourceInit);
    }

    @Override
    public void afterScope(BQTestScope scope, ExtensionContext context) {
        dataSourceHolder.close();
    }

    @Override
    public void beforeMethod(BQTestScope scope, ExtensionContext context) {
        if (deleteTablesInInsertOrder != null && deleteTablesInInsertOrder.length > 0) {
            new DataManager(getConnector(), deleteTablesInInsertOrder).deleteData();
        }
    }

    protected void afterDataSourceInit() {
        initConnector();
        execInitFunction();
        execInitScript();
        execLiquibaseMigrations();
    }
}
