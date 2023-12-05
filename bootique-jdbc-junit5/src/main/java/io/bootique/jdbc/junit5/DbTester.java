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

import io.bootique.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Key;
import io.bootique.jdbc.junit5.connector.DbConnector;
import io.bootique.jdbc.junit5.sql.ExecStatementBuilder;
import io.bootique.jdbc.junit5.datasource.DataSourceHolder;
import io.bootique.jdbc.junit5.datasource.DriverDataSource;
import io.bootique.jdbc.junit5.init.DbInitializer;
import io.bootique.jdbc.junit5.metadata.DbMetadata;
import io.bootique.jdbc.junit5.script.SqlScriptRunner;
import io.bootique.jdbc.junit5.tester.DataManager;
import io.bootique.jdbc.junit5.tester.DataSourcePropertyBuilder;
import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.scope.BQAfterScopeCallback;
import io.bootique.junit5.scope.BQBeforeMethodCallback;
import io.bootique.junit5.scope.BQBeforeScopeCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.sql.DataSource;
import java.sql.Connection;
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

    protected final DbInitializer initializer;
    protected String[] deleteTablesInInsertOrder;

    protected final DataSourceHolder dataSourceHolder;
    protected DbConnector connector;

    public DbTester() {
        this.dataSourceHolder = new DataSourceHolder();
        this.initializer = new DbInitializer();
    }

    public DataSource getDataSource() {
        return dataSourceHolder;
    }

    public String getDbUrl() {
        return dataSourceHolder.getDbUrl();
    }

    /**
     * Returns test DB "connector" that is a wrapper around DB DataSource providing access to DB metadata, and various
     * query builders. In most cases instead of DbConnector, you should use {@link Table} (via {@link #getTable(String)})
     * to work with the DB. Connector is only needed to run SQL directly.
     *
     * @since 2.0
     */
    public DbConnector getConnector() {
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
     * Executes provided SQL script after the DB startup. The script would usually contain database schema and test
     * data. Assumes statements are separated with ";" character.
     *
     * @param initDBScript a location of the SQL script in Bootique {@link io.bootique.resource.ResourceFactory} format.
     * @return this tester
     */
    public SELF initDB(String initDBScript) {
        return initDB(initDBScript, null);
    }

    /**
     * Executes provided SQL script after the DB startup. The script would usually contain database schema and test
     * data.
     *
     * @param initDBScript a location of the SQL script in Bootique {@link io.bootique.resource.ResourceFactory} format.
     * @param delimiter    SQL statements delimiter in the "initDBScript". An explicit delimiter may be useful when
     *                     the file contains common DB delimiters in the middle of stored procedure declarations, etc.
     * @return this tester
     */
    public SELF initDB(String initDBScript, String delimiter) {
        initializer.addScript(initDBScript, delimiter);
        return (SELF) this;
    }

    public SELF initDB(JdbcOp initFunction) {
        initializer.addFunction(initFunction);
        return (SELF) this;
    }

    /**
     * @deprecated since 2.0.B1 in favor of {@link #initDBWithLiquibaseChangelog(String)}
     */
    @Deprecated
    public SELF runLiquibaseMigrations(String liquibaseChangeLog) {
        return initDBWithLiquibaseChangelog(liquibaseChangeLog);
    }

    /**
     * @deprecated since 2.0.B1 in favor of {@link #initDBWithLiquibaseChangelog(String, String)}
     */
    @Deprecated
    public SELF runLiquibaseMigrations(String liquibaseChangeLog, String liquibaseContext) {
        return initDBWithLiquibaseChangelog(liquibaseChangeLog, liquibaseContext);
    }

    /**
     * Schedules execution of a Liquibase changelog file after DB startup.
     *
     * @param changelog a location of the Liquibase changelog file in Bootique
     *                  {@link io.bootique.resource.ResourceFactory} format.
     * @return this tester
     * @since 2.0
     */
    public SELF initDBWithLiquibaseChangelog(String changelog) {
        return initDBWithLiquibaseChangelog(changelog, null);
    }

    /**
     * Schedules execution of a Liquibase changelog file after DB startup.
     *
     * @param changelog        a location of the Liquibase changelog file in Bootique
     *                         {@link io.bootique.resource.ResourceFactory} format.
     * @param liquibaseContext Liquibase context expression to filter migrations as appropriate for the test run.
     * @return this tester
     * @since 2.0
     */
    public SELF initDBWithLiquibaseChangelog(String changelog, String liquibaseContext) {
        initializer.addLiquibase(changelog, liquibaseContext);
        return (SELF) this;
    }

    /**
     * Executes provided SQL script. Assumes statements in the script are separated with ";" character.
     *
     * @param script a location of the SQL script in Bootique {@link io.bootique.resource.ResourceFactory} format.
     * @since 2.0
     */
    public void runScript(String script) {
        runScript(script, null);
    }

    /**
     * Executes provided SQL script. The script would usually contain database schema and test
     * data.
     *
     * @param script    a location of the SQL script in Bootique {@link io.bootique.resource.ResourceFactory} format.
     * @param delimiter Optional SQL statements delimiter in the "script". When null, a semicolon is assumed. An
     *                  explicit delimiter may be useful when the file contains common DB delimiters in the middle of
     *                  stored procedure declarations, etc.
     */
    public void runScript(String script, String delimiter) {

        Objects.requireNonNull(script, "Null 'script'");
        new SqlScriptRunner(script).delimiter(delimiter).run(dataSourceHolder);
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

    protected abstract DriverDataSource createNonPoolingDataSource(BQTestScope scope);

    @Override
    public void beforeScope(BQTestScope scope, ExtensionContext context) {

        // By now the DataSource may already be initialized
        // if BQRuntime using DbTester had some eager dependencies on DataSource

        dataSourceHolder.initIfNeeded(() -> createNonPoolingDataSource(scope), this::afterDataSourceInit);
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
        initDB();
    }

    protected void initConnector() {
        this.connector = new DbConnector(dataSourceHolder, DbMetadata.create(dataSourceHolder));
    }

    protected void initDB() {
        initializer.exec(dataSourceHolder);
    }
}
