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
package io.bootique.jdbc.junit5.datasource;

import org.junit.jupiter.api.extension.ExtensionContext;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * A DataSource wrapper that allows to pass around a DataSource before it is fully initialized. Needed for deferred
 * DataSource initialization within JUnit 5 lifecycle.
 *
 * @since 2.0.B1
 */
public class DataSourceHolder implements DataSource, ExtensionContext.Store.CloseableResource {

    private DataSource dataSource;

    public void initIfNeeded(Supplier<DataSource> dataSourceSupplier, Runnable runAfterInit) {
        if (this.dataSource == null) {
            synchronized (this) {
                if (this.dataSource == null) {
                    this.dataSource = dataSourceSupplier.get();
                    runAfterInit.run();
                }
            }
        }
    }

    @Override
    public void close() throws Throwable {
        if (dataSource != null && dataSource instanceof ExtensionContext.Store.CloseableResource) {
            ((ExtensionContext.Store.CloseableResource) dataSource).close();
        }
    }

    protected DataSource nonNullDataSource() {
        return Objects.requireNonNull(dataSource, "'dataSource' not initialized. Called outside of JUnit lifecycle?");
    }

    @Override
    public Connection getConnection() throws SQLException {
        return nonNullDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return nonNullDataSource().getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return nonNullDataSource().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        nonNullDataSource().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        nonNullDataSource().setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return nonNullDataSource().getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return nonNullDataSource().getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return nonNullDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return nonNullDataSource().isWrapperFor(iface);
    }
}
