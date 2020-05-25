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
import io.bootique.jdbc.junit5.tester.*;
import io.bootique.jdbc.junit5.connector.DbConnector;
import io.bootique.jdbc.junit5.datasource.PoolingDataSource;
import io.bootique.jdbc.junit5.datasource.PoolingDataSourceParameters;
import io.bootique.jdbc.junit5.connector.ExecStatementBuilder;
import io.bootique.jdbc.junit5.connector.SelectStatementBuilder;
import io.bootique.jdbc.junit5.metadata.DbMetadata;
import io.bootique.resource.ResourceFactory;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A helper class that is declared in a unit test and manages database startup, schema and data initialization and
 * shutdown.
 *
 * @since 2.0
 */
public abstract class DbTester implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbTester.class);

    protected ResourceFactory initDBScript;
    protected String initDBScriptDelimiter;
    protected String[] deleteTablesInInsertOrder;

    protected PoolingDataSource dataSource;
    protected DbConnector connector;

    /**
     * Creates a tester that will bootstrap a DB using Docker/Testcontainers.
     *
     * @return a new tester instance
     * @see <a href="https://www.testcontainers.org/modules/databases/jdbc/">Testcontainers JDBC URLs</a>
     */
    public static DbTester testcontainersDb(String containerDbUrl) {
        return new TestcontainersTester(containerDbUrl);
    }

    /**
     * Creates a tester that will use in-memory Derby DB, with DB files stored in a temporary directory.
     *
     * @return a new tester instance
     */
    public static DbTester derbyDb() {

        Path[] tempFir = new Path[1];
        assertDoesNotThrow(() -> {
            tempFir[0] = Files.createTempDirectory("io.bootique.jdbc.test.derby-db");
        });

        return new DerbyTester(tempFir[0].toFile());
    }

    public DataSource getDataSource() {
        assertNotNull(dataSource, "DataSource is not initialized. Called outside of test lifecycle?");
        return dataSource;
    }

    protected DbConnector getConnector() {
        assertNotNull(connector, "DbConnector is not initialized. Called outside of test lifecycle?");
        return connector;
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

    public <T> SelectStatementBuilder<T> selectStatement(RowReader<T> rowReader) {
        return getConnector().selectStatement(rowReader);
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
     * data.
     *
     * @param initDBScript a location of the SQL script in Bootique {@link io.bootique.resource.ResourceFactory} format.
     * @return this tester
     */
    public DbTester initDB(String initDBScript) {
        return initDB(initDBScript, null);
    }

    /**
     * Executes a provided SQL script after the DB startup. The script would usually contain database schema and test
     * data.
     *
     * @param initDBScript a location of the SQL script in Bootique {@link io.bootique.resource.ResourceFactory} format.
     * @param delimiter    SQL statements delimiter in the "initDBScript". AN explicit delimiter may be useful when
     *                     the file contains common DB delimiters in the middle of stored procedure declartations, etc.
     * @return this tester
     */
    public DbTester initDB(String initDBScript, String delimiter) {
        this.initDBScript = new ResourceFactory(initDBScript);
        this.initDBScriptDelimiter = delimiter;
        return this;
    }

    /**
     * Configures the Tester to delete data from the specified tables before each test.
     *
     * @param tablesInInsertOrder a list of table names in the order of INSERT dependencies between them.
     * @return this tester
     */
    public DbTester deleteBeforeEachTest(String... tablesInInsertOrder) {
        this.deleteTablesInInsertOrder = tablesInInsertOrder;
        return this;
    }

    /**
     * Returns a Bootique module that can be used to configure a test DataSource in test Bootique runtime.
     *
     * @param dataSourceName the name of the DataSource
     * @return a new Bootique module with test DataSource configuration.
     */
    public BQModule setOrReplaceDataSource(String dataSourceName) {
        return binder -> configure(binder, dataSourceName);
    }

    protected PoolingDataSource createDataSource() {
        PoolingDataSourceParameters parameters = new PoolingDataSourceParameters();
        parameters.setMaxConnections(5);
        parameters.setMinConnections(1);
        parameters.setMaxQueueWaitTime(20000);

        return new PoolingDataSource(createNonPoolingDataSource(), parameters);
    }

    protected abstract DataSource createNonPoolingDataSource();

    protected void execInitScript() {
        if (initDBScript != null) {

            LOGGER.info("initializing DB from {}", initDBScript.getUrl());
            String delimiter = this.initDBScriptDelimiter != null ? this.initDBScriptDelimiter : ";";
            Iterable<String> statements = new SqlScriptParser("--", "/*", "*/", delimiter).getStatements(initDBScript);

            try (Connection c = dataSource.getConnection()) {

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

    @Override
    public void beforeAll(ExtensionContext context) {
        this.dataSource = createDataSource();
        this.connector = new DbConnector(dataSource, DbMetadata.create(dataSource));
        execInitScript();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        // can be null if failed to start
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        if (deleteTablesInInsertOrder != null && deleteTablesInInsertOrder.length > 0) {
            new DataManager(getConnector(), deleteTablesInInsertOrder).deleteData();
        }
    }
}