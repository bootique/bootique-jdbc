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
import io.bootique.junit5.BQTestScope;
import org.junit.jupiter.api.Assertions;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @since 2.0
 */
public class TestcontainersTester extends DbTester {

    private static final Pattern TC_REUSABLE_PATTERN = Pattern.compile("&?TC_REUSABLE=([^\\?&]+)");

    private final String containerDbUrl;

    public TestcontainersTester(String containerDbUrl) {
        this.containerDbUrl = Objects.requireNonNull(containerDbUrl);
    }

    @Override
    protected DataSource createNonPoolingDataSource(BQTestScope scope) {
        Assertions.assertDoesNotThrow(
                () -> Class.forName("org.testcontainers.jdbc.ContainerDatabaseDriver"),
                "Error loading testcontainers JDBC driver");

        return new DriverDataSource(null, dbUrl(scope), null, null);
    }

    protected String dbUrl(BQTestScope scope) {

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
