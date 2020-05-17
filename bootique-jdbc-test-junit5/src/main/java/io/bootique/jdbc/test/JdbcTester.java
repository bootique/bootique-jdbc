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

import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.jdbc.JdbcModule;
import io.bootique.jdbc.test.datasource.PoolingDataSourceParameters;
import io.bootique.jdbc.test.datasource.PoolingDataSource;
import io.bootique.jdbc.test.tester.*;
import io.bootique.resource.ResourceFactory;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.sql.DataSource;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A helper class that is declared in a unit test and manages database startup, schema and data initialization and
 * shutdown. Currently works only with Hikari DataSource provider.
 *
 * @since 2.0
 */
public abstract class JdbcTester implements BeforeAllCallback {

    protected ResourceFactory initDBScript;
    protected ExtensionContext.Store dataSourceStore;

    /**
     * Creates a tester that will bootstrap a DB using Docker/Testcontainers.
     *
     * @return this tester
     * @see <a href="https://www.testcontainers.org/modules/databases/jdbc/">Testcontainers JDBC URLs</a>
     */
    public static JdbcTester useTestcontainers(String containerDbUrl) {
        return new TestcontainersTester(containerDbUrl);
    }

    public static JdbcTester useDerby() {
        // TODO: Don't assume Maven, use deletable temp dir.
        return new DerbyTester(new File("target/derby"));
    }

    protected void configure(Binder binder, String dataSourceName) {
        configureDataSource(binder, dataSourceName);
        configureInitFunction(binder);
    }

    protected void configureDataSource(Binder binder, String dataSourceName) {

        assertNotNull(dataSourceStore);
        JdbcTesterState.bindToBootique(binder, dataSourceStore);

        // TODO: we need to handle the case when this is overlayed over the existing configuration that will
        //  have other properties unsupported by "bqjdbctest" factory
        DataSourcePropertyBuilder.create(binder, dataSourceName).property("type", "bqjdbctest");
    }

    protected void configureInitFunction(Binder binder) {
        if (initDBScript != null) {
            JdbcModule.extend(binder).addDataSourceListener(new InitDBListener(initDBScript));
        }
    }

    /**
     * Executes a provided SQL script after the DB startup. The script would usually contain database schema and test
     * data.
     *
     * @param initDBScript a location of the SQL script in Bootique {@link io.bootique.resource.ResourceFactory} format.
     * @return this tester
     */
    public JdbcTester initDB(String initDBScript) {
        this.initDBScript = new ResourceFactory(initDBScript);
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

    @Override
    public void beforeAll(ExtensionContext context) {
        // PoolingDataSource is a ClosableResource, so it will be disposed of by JUnit when the store goes out of scope
        this.dataSourceStore = JdbcTesterState.saveDataSource(context, createDataSource());
    }

    protected PoolingDataSource createDataSource() {
        PoolingDataSourceParameters parameters = new PoolingDataSourceParameters();
        parameters.setMaxConnections(5);
        parameters.setMinConnections(1);
        parameters.setMaxQueueWaitTime(20000);

        return new PoolingDataSource(createNonPoolingDataSource(), parameters);
    }

    protected abstract DataSource createNonPoolingDataSource();
}
