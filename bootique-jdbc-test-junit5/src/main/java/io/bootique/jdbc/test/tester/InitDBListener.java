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

import io.bootique.jdbc.DataSourceListener;
import io.bootique.resource.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class InitDBListener implements DataSourceListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitDBListener.class);

    private ResourceFactory initScript;

    public InitDBListener(ResourceFactory initScript) {
        this.initScript = Objects.requireNonNull(initScript);
    }

    @Override
    public void afterStartup(String name, String jdbcUrl, DataSource dataSource) {
        LOGGER.info("initializing DB from {}", initScript.getUrl());

        Iterable<String> statements = new SqlScriptParser("--", "/*", "*/", ";").getStatements(initScript);

        try (Connection c = dataSource.getConnection()) {

            for (String sql : statements) {
                try (PreparedStatement statement = c.prepareStatement(sql)) {
                    statement.execute();
                } catch (SQLException e) {
                    throw new RuntimeException("Error running SQL statement " + sql + ": " + e.getMessage(), e);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error running SQL from " + initScript.getUrl() + ": " + e.getMessage(), e);
        }
    }
}
