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

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class DriverDataSourceTest {

    @Test
    public void testIsWrapperFor() {

        DriverDataSource ds = new DriverDataSource(null, "jdbc:derby:target/derby/bla", null, null);

        boolean wrapsSelf = ds.isWrapperFor(DriverDataSource.class);
        assertTrue(wrapsSelf);

        boolean wrapsDS1 = ds.isWrapperFor(DS1.class);
        assertFalse(wrapsDS1);
    }

    @Test
    public void testUnwrap() throws SQLException {

        DriverDataSource ds = new DriverDataSource(null, "jdbc:derby:target/derby/bla", null, null);

        DataSource unwrappedSelf = ds.unwrap(DriverDataSource.class);
        assertSame(ds, unwrappedSelf);
    }

    static class DS1 implements DataSource {

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            if (!DS1.class.equals(iface)) {
                throw new SQLException();
            }

            return (T) this;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return DS1.class.equals(iface);
        }

        @Override
        public Connection getConnection() throws SQLException {
            return null;
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return null;
        }

        @Override
        public PrintWriter getLogWriter() {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) {

        }

        @Override
        public void setLoginTimeout(int seconds) {

        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public Logger getParentLogger() {
            return null;
        }
    }
}
