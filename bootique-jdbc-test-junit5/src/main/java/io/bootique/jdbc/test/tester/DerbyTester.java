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

import io.bootique.jdbc.test.JdbcTester;
import io.bootique.jdbc.test.datasource.DriverDataSource;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.OutputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @since 2.0
 */
public class DerbyTester extends JdbcTester {

    private static final Logger LOGGER = LoggerFactory.getLogger(DerbyTester.class);

    public static final OutputStream DEV_NULL = new OutputStream() {
        @Override
        public void write(int b) {
        }
    };
    private static final Pattern DERBY_URL_PATTERN = Pattern.compile("^jdbc:derby:([^;:]+)");

    private final AtomicInteger dbId;
    private final File baseDirectory;

    public DerbyTester(File baseDirectory) {
        this.baseDirectory = Objects.requireNonNull(baseDirectory);
        this.dbId = new AtomicInteger(0);
    }

    @Override
    protected DataSource createNonPoolingDataSource() {
        String url = jdbcUrl();
        beforeDerbyStartup(url);
        return new DriverDataSource(null, url, null, null);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        super.afterAll(context);
        afterDerbyShutdown();
    }

    protected String jdbcUrl() {
        return String.format("jdbc:derby:%s/db_%s;create=true", baseDirectory, dbId.getAndIncrement());
    }

    protected void afterDerbyShutdown() {
        LOGGER.info("Stopping all JVM Derby servers...");

        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            // the exception is actually expected on shutdown... go figure...
        }
    }

    protected void beforeDerbyStartup(String jdbcUrl) {

        LOGGER.info("Preparing Derby server at '{}'...", jdbcUrl);
        File dbDir = getDbDir(jdbcUrl);
        if (dbDir != null) {
            deleteDir(dbDir);
        }

        // Need to reload the driver if there was a previous shutdown
        // see https://db.apache.org/derby/docs/10.5/devguide/tdevdvlp20349.html
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        } catch (Exception e) {
            fail("Failed to load Derby driver");
        }
    }

    protected void deleteDir(File dir) {
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

    protected static File getDbDir(String jdbcUrl) {
        Matcher m = DERBY_URL_PATTERN.matcher(jdbcUrl);
        return m.find() ? new File(m.group(1)) : null;
    }
}
