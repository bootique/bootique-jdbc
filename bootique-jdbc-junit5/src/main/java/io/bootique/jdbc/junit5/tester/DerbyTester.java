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
package io.bootique.jdbc.junit5.tester;

import io.bootique.jdbc.junit5.DbTester;
import io.bootique.jdbc.junit5.datasource.DriverDataSource;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.OutputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @since 2.0
 */
public class DerbyTester extends DbTester {

    private static final Logger LOGGER = LoggerFactory.getLogger(DerbyTester.class);

    // called via reflection, so looks unused
    public static final OutputStream DEV_NULL = new OutputStream() {
        @Override
        public void write(int b) {
        }
    };

    private final File derbyFolder;
    private final String jdbcUrl;

    public DerbyTester(File derbyFolder) {
        this.derbyFolder = Objects.requireNonNull(derbyFolder);
        this.jdbcUrl = String.format("jdbc:derby:%s;create=true", derbyFolder);

        // suppressing derby.log in "user.dir".
        if (System.getProperty("derby.stream.error.field") == null) {
            System.setProperty("derby.stream.error.field", DerbyTester.class.getName() + ".DEV_NULL");
        }
    }

    @Override
    protected DataSource createNonPoolingDataSource() {
        prepareForDerbyStartup();
        return new DriverDataSource(null, jdbcUrl, null, null);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        super.afterAll(context);
        performDerbyShutdown();
    }

    protected void prepareForDerbyStartup() {

        LOGGER.info("Preparing Derby server at '{}'...", derbyFolder);
        deleteDir(derbyFolder);

        // Need to reload the driver if there was a previous shutdown
        // see https://db.apache.org/derby/docs/10.5/devguide/tdevdvlp20349.html
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        } catch (Exception e) {
            fail("Failed to load Derby driver");
        }
    }

    protected void performDerbyShutdown() {
        LOGGER.info("Stopping all JVM Derby servers...");

        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            // the exception is actually expected on shutdown... go figure...
        }

        deleteDir(derbyFolder);
    }

    protected static void deleteDir(File dir) {
        if (dir.exists()) {

            for (File f : dir.listFiles()) {
                if (f.isFile()) {
                    assertTrue(f.delete());
                } else {
                    deleteDir(f);
                }
            }

            assertTrue(dir.delete());
        }
    }
}
