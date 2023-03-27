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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Properties;

/**
 * A non-pooling DataSource implementation wrapping a JDBC driver.
 *
 * @since 2.0
 */
// courtesy of Apache Cayenne project
public class DriverDataSource implements DataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverDataSource.class);

    protected Driver driver;
    protected String dbUrl;
    protected String userName;
    protected String password;

    public DriverDataSource(Driver driver, String dbUrl, String userName, String password) {

        if (dbUrl == null) {
            throw new NullPointerException("Null 'connectionUrl'");
        }

        this.driver = driver;
        this.dbUrl = dbUrl;
        this.userName = userName;
        this.password = password;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    @Override
    public Connection getConnection() throws SQLException {
        // login with internal credentials
        return getConnection(userName, password);
    }

    @Override
    public Connection getConnection(String userName, String password) throws SQLException {
        try {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Connecting to '{}' as '{}'", dbUrl, userName);
            }

            Connection c;

            if (driver == null) {
                c = DriverManager.getConnection(dbUrl, userName, password);
            } else {
                Properties connectProperties = new Properties();

                if (userName != null) {
                    connectProperties.put("user", userName);
                }

                if (password != null) {
                    connectProperties.put("password", password);
                }
                c = driver.connect(dbUrl, connectProperties);
            }

            // some drivers (Oracle) return null connections instead of throwing an exception... fix it here

            if (c == null) {
                throw new SQLException("Can't establish connection: " + dbUrl);
            }

            LOGGER.debug("+++ Connecting: SUCCESS.");

            return c;
        } catch (SQLException ex) {
            LOGGER.warn("*** Connecting: FAILURE.", ex);
            throw ex;
        }
    }

    @Override
    public int getLoginTimeout() {
        return -1;
    }

    @Override
    public void setLoginTimeout(int seconds) {
        // noop
    }

    @Override
    public PrintWriter getLogWriter() {
        return DriverManager.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        DriverManager.setLogWriter(out);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(this);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!iface.isInstance(this)) {
            throw new SQLException("Can't unwrap " + iface);
        }

        return (T) this;
    }

    @Override
    public java.util.logging.Logger getParentLogger() {
        throw new UnsupportedOperationException();
    }
}

