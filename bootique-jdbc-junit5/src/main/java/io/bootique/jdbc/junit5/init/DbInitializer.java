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
package io.bootique.jdbc.junit5.init;

import io.bootique.jdbc.junit5.JdbcOp;
import io.bootique.jdbc.junit5.script.SqlScriptRunner;
import io.bootique.jdbc.liquibase.LiquibaseRunner;
import io.bootique.resource.ResourceFactory;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @since 2.0
 */
public class DbInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbInitializer.class);

    private final List<Consumer<DataSource>> initSteps;

    public DbInitializer() {
        this.initSteps = new ArrayList<>();
    }

    public void addScript(String initDBScript, String delimiter) {
        Objects.requireNonNull(initDBScript, "Null 'initDBScript'");
        ResourceFactory initDBScriptResource = new ResourceFactory(initDBScript);
        Consumer<DataSource> step = ds -> execScript(ds, initDBScriptResource, delimiter);

        initSteps.add(step);
    }

    public void addFunction(JdbcOp initFunction) {
        Objects.requireNonNull(initFunction, "Null 'initFunction'");
        Consumer<DataSource> step = ds -> execJdbcOp(ds, initFunction);

        initSteps.add(step);
    }

    public void addLiquibase(String changelog, String liquibaseContext) {
        Objects.requireNonNull(changelog, "Null 'changelog'");
        ResourceFactory changelogResource = new ResourceFactory(changelog);
        Consumer<DataSource> step = ds -> execLiquibase(ds, changelogResource, liquibaseContext);

        initSteps.add(step);
    }

    public void exec(DataSource dataSource) {
        initSteps.forEach(s -> s.accept(dataSource));
    }

    protected void execLiquibase(DataSource dataSource, ResourceFactory changelog, String liquibaseContext) {
        LOGGER.info("running init Liquibase changelog {}", changelog.getUrl());
        Contexts contexts = liquibaseContext != null ? new Contexts(liquibaseContext) : new Contexts();
        new LiquibaseRunner(Collections.singletonList(changelog), dataSource, null).run(lb -> execLiquibase(lb, contexts));
    }

    protected void execLiquibase(Liquibase lb, Contexts contexts) {
        try {
            lb.update(contexts, new LabelExpression());
        } catch (Exception e) {
            throw new RuntimeException("Error running migrations against the test DB", e);
        }
    }

    protected void execJdbcOp(DataSource dataSource, JdbcOp op) {
        LOGGER.info("running init function");

        try (Connection c = dataSource.getConnection()) {
            op.run(c);
        } catch (SQLException e) {
            throw new RuntimeException("Error running init function: " + e.getMessage(), e);
        }
    }

    protected void execScript(DataSource dataSource, ResourceFactory script, String delimiter) {
        LOGGER.info("running init script {}", script.getUrl());
        new SqlScriptRunner(script).delimiter(delimiter).run(dataSource);
    }
}
