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
package io.bootique.jdbc.junit5.derby;

import io.bootique.jdbc.junit5.DbTester;
import io.bootique.jdbc.junit5.datasource.DriverDataSource;
import io.bootique.junit5.BQTestScope;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @since 2.0
 */
public class DerbyTester extends DbTester<DerbyTester> {

    static {
        // Since Derby Driver is on classpath, even if Derby is not in use in the tests, it generates an empty
        // "derby.log". Let's get rid of it preemptively. TODO: dirty
        DerbyTester.sendDerbyLogsToDevNull();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DerbyTester.class);

    // called via reflection in 'devNullLogger', so looks unused
    public static final OutputStream DEV_NULL = new OutputStream() {
        @Override
        public void write(int b) {
        }
    };

    protected static void sendDerbyLogsToDevNull() {
        // suppressing derby.log in "user.dir".
        if (System.getProperty("derby.stream.error.field") == null) {
            System.setProperty("derby.stream.error.field", DerbyTester.class.getName() + ".DEV_NULL");
        }
    }

    private final File derbyFolder;
    private final String dbUrl;

    /**
     * Creates a tester that will use in-memory Derby DB, with DB files stored in a unique temporary directory.
     *
     * @return a new tester instance
     */
    public static DerbyTester db() {

        Path[] tempFir = new Path[1];
        assertDoesNotThrow(() -> {
            tempFir[0] = Files.createTempDirectory("io.bootique.jdbc.test.derby-db");
        });

        return db(tempFir[0].toFile());
    }

    /**
     * Creates a tester that will use in-memory Derby DB, with DB files stored in under the provided directory in a
     * subfolder called "derby".
     *
     * @return a new tester instance
     * @since 3.0
     */
    public static DerbyTester db(String derbyFolder) {
        return new DerbyTester(new File(derbyFolder));
    }

    /**
     * Creates a tester that will use in-memory Derby DB, with DB files stored in under the provided directory in a
     * subfolder called "derby".
     *
     * @return a new tester instance
     * @since 3.0
     */
    public static DerbyTester db(File derbyFolder) {
        return new DerbyTester(derbyFolder);
    }

    /**
     * @deprecated use {@link #db(File)}. This constructor will be made non-public eventually.
     */
    @Deprecated(since = "3.0")
    public DerbyTester(File derbyFolder) {
        this.derbyFolder = Objects.requireNonNull(derbyFolder);

        // placing Derby in subfolder, so that the presence of the parent folder is not in the way of starting the DB
        this.dbUrl = String.format("jdbc:derby:%s/derby;create=true", derbyFolder);

        sendDerbyLogsToDevNull();
    }

    @Override
    protected DriverDataSource createNonPoolingDataSource(BQTestScope scope) {
        prepareForDerbyStartup();
        return new DriverDataSource(null, dbUrl, null, null);
    }

    @Override
    public void afterScope(BQTestScope scope, ExtensionContext context) {
        performDerbyShutdown();
    }

    protected void prepareForDerbyStartup() {

        LOGGER.info("Preparing Derby server at '{}'...", derbyFolder);
        deleteDirContents(derbyFolder);

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

    protected static void deleteDirContents(File dir) {
        if (dir.exists()) {

            for (File f : dir.listFiles()) {
                if (f.isFile()) {
                    assertTrue(f.delete());
                } else {
                    deleteDir(f);
                }
            }
        }
    }

    protected static void deleteDir(File dir) {
        if (dir.exists()) {
            deleteDirContents(dir);
            assertTrue(dir.delete());
        }
    }
}
