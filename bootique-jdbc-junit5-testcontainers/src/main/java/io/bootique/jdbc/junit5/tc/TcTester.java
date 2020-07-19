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
package io.bootique.jdbc.junit5.tc;

import io.bootique.jdbc.junit5.DbTester;
import io.bootique.jdbc.junit5.datasource.DriverDataSource;
import io.bootique.junit5.BQTestScope;
import org.junit.jupiter.api.Assertions;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A DbTester based on testcontainers library.
 *
 * @since 2.0
 */
public class TcTester extends DbTester<TcTester> {

    private static final Pattern TC_REUSABLE_PATTERN = Pattern.compile("&?TC_REUSABLE=([^\\?&]+)");

    private final String containerDbUrl;
    private final JdbcDatabaseContainer container;
    private final String userName;
    private final String password;

    /**
     * Creates a tester that will bootstrap a DB using Docker/Testcontainers. If the tester is executed in the "global"
     * scope (per {@link io.bootique.junit5.BQTestTool} annotation), the tester will alter "containerDbUrl" internally
     * before passing it to Testcontainers, forcing a "TC_REUSABLE=true" parameter regardless of its presence
     * or value in the original String.
     *
     * @param containerDbUrl a Testcontainers DB URL
     * @return a new tester instance
     * @see <a href="https://www.testcontainers.org/modules/databases/jdbc/">Testcontainers JDBC URLs</a>
     */
    public static TcTester db(String containerDbUrl) {
        return new TcTester(containerDbUrl, null, null);
    }

    public static TcTester db(JdbcDatabaseContainer container) {
        return new TcTester(container, null, null);
    }

    public static TcTester db(JdbcDatabaseContainer container, String userName, String password) {
        return new TcTester(container, userName, password);
    }

    protected TcTester(String containerDbUrl, String userName, String password) {
        this.containerDbUrl = Objects.requireNonNull(containerDbUrl);
        this.container = null;
        this.userName = userName;
        this.password = password;
    }

    protected TcTester(JdbcDatabaseContainer container, String userName, String password) {
        this.container = Objects.requireNonNull(container);
        this.containerDbUrl = null;
        this.userName = userName;
        this.password = password;
    }

    @Override
    protected DataSource createNonPoolingDataSource(BQTestScope scope) {
        Assertions.assertDoesNotThrow(
                () -> Class.forName("org.testcontainers.jdbc.ContainerDatabaseDriver"),
                "Error loading testcontainers JDBC driver");

        return new DriverDataSource(null, dbUrl(scope), dbUser(), dbPassword());
    }

    protected String dbUser() {
        return userName != null
                ? userName
                : container != null ? container.getUsername() : null;
    }

    protected String dbPassword() {
        return password != null
                ? password
                : container != null ? container.getPassword() : null;
    }

    protected String dbUrl(BQTestScope scope) {
        // ignoring scope if the URL is container provided. Any way to check the container scope and ensure it matches
        // ours?
        return container != null ? container.getJdbcUrl() : urlBaseDbUrl(scope);
    }

    protected String urlBaseDbUrl(BQTestScope scope) {

        // Ensure that Testcontainers doesn't shut down the container underneath DbTester running in a global scope.
        // Generally keeping the connection pool around is sufficient to prevent shutdown (as it keeps some open
        // connections), but let's not rely on side effects for this, and set TC_REUSABLE=true explicitly.

        switch (scope) {
            case GLOBAL:
                return reusableContainerDbUrl(containerDbUrl);
            default:
                return containerDbUrl;
        }
    }

    protected String reusableContainerDbUrl(String url) {
        String reusableParam = "TC_REUSABLE=true";
        String andReusableParam = "&" + reusableParam;

        int q = url.indexOf('?');
        if (q < 0) {
            return url + "?" + reusableParam;
        } else if (q == url.length() - 1) {
            return url + reusableParam;
        }

        Matcher m = TC_REUSABLE_PATTERN.matcher(url.substring(q + 1));
        if (!m.find()) {
            return url + andReusableParam;
        }

        StringBuffer out = new StringBuffer().append(url, 0, q + 1);
        do {
            m.appendReplacement(out, m.group().startsWith("&") ? andReusableParam : reusableParam);
        } while (m.find());

        m.appendTail(out);

        return out.toString();
    }
}
