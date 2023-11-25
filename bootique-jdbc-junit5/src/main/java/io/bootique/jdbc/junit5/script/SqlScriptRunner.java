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
package io.bootique.jdbc.junit5.script;

import io.bootique.resource.ResourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * @since 2.0
 */
public class SqlScriptRunner {

    private static final String DEFAULT_DELIMITER = ";";
    private static final String DEFAULT_COMMENT_PREFIX = "--";
    private static final String DEFAULT_BLOCK_COMMENT_START = "/*";
    private static final String DEFAULT_BLOCK_COMMENT_END = "*/";

    private String commentPrefix;
    private String blockCommentStart;
    private String blockCommentEnd;
    private String delimiter;
    private final ResourceFactory script;

    public SqlScriptRunner(String scriptResource) {
        this(new ResourceFactory(Objects.requireNonNull(scriptResource, "Null 'scriptResource'")));
    }

    public SqlScriptRunner(ResourceFactory script) {
        this.script = Objects.requireNonNull(script, "Null 'script'");
    }

    public SqlScriptRunner blockCommentStart(String blockCommentStart) {
        this.blockCommentStart = blockCommentStart;
        return this;
    }

    public SqlScriptRunner blockCommentEnd(String blockCommentEnd) {
        this.blockCommentEnd = blockCommentEnd;
        return this;
    }

    public SqlScriptRunner commentPrefix(String commentPrefix) {
        this.commentPrefix = commentPrefix;
        return this;
    }

    public SqlScriptRunner delimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public void run(DataSource dataSource) {

        String cp = commentPrefix != null ? commentPrefix : DEFAULT_COMMENT_PREFIX;
        String bcs = blockCommentStart != null ? blockCommentStart : DEFAULT_BLOCK_COMMENT_START;
        String bce = blockCommentEnd != null ? blockCommentEnd : DEFAULT_BLOCK_COMMENT_END;
        String d = delimiter != null ? delimiter : DEFAULT_DELIMITER;

        List<String> statements = new SqlScriptParser(cp, bcs, bce, d).getStatements(script);

        if (!statements.isEmpty()) {

            try (Connection c = dataSource.getConnection()) {

                for (String sql : statements) {
                    try (PreparedStatement statement = c.prepareStatement(sql)) {
                        statement.execute();
                    } catch (SQLException e) {
                        throw new RuntimeException("Error running SQL statement " + sql + ": " + e.getMessage(), e);
                    }
                }

            } catch (SQLException e) {
                throw new RuntimeException("Error running SQL from " + script.getUrl() + ": " + e.getMessage(), e);
            }
        }
    }
}
