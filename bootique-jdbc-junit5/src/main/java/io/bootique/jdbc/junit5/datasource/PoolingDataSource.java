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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;

/**
 * A non-blocking {@link DataSource} with a pool of connections.
 *
 * @since 2.0
 */
// courtesy of Apache Cayenne project
public class PoolingDataSource implements DataSource, ExtensionContext.Store.CloseableResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoolingDataSource.class);

    private DataSource nonPoolingDataSource;
    private long maxQueueWaitTime;

    private Map<PoolAwareConnection, Object> pool;
    private Semaphore poolCap;
    private BlockingQueue<PoolAwareConnection> available;
    private String validationQuery;

    public PoolingDataSource(DataSource nonPoolingDataSource, PoolingDataSourceParameters parameters) {

        int minConnections = parameters.getMinConnections();
        int maxConnections = parameters.getMaxConnections();

        // sanity check
        if (minConnections < 0) {
            throw new IllegalArgumentException("Negative min connections: " + minConnections);
        }

        if (maxConnections < 0) {
            throw new IllegalArgumentException("Negative max connections: " + maxConnections);
        }

        if (minConnections > maxConnections) {
            throw new IllegalArgumentException("Min connections (" + minConnections
                    + ") is greater than max connections (" + maxConnections + ")");
        }

        this.nonPoolingDataSource = nonPoolingDataSource;
        this.maxQueueWaitTime = parameters.getMaxQueueWaitTime();
        this.validationQuery = parameters.getValidationQuery();
        this.pool = new ConcurrentHashMap<>((int) (maxConnections / 0.75));
        this.available = new ArrayBlockingQueue<>(maxConnections);
        this.poolCap = new Semaphore(maxConnections);

        // grow pool to min connections
        try {
            for (int i = 0; i < minConnections; i++) {
                PoolAwareConnection c = createUnchecked();
                reclaim(c);
            }
        } catch (BadValidationQueryException e) {
            throw new RuntimeException("Bad validation query: " + validationQuery, e);
        } catch (SQLException e) {
            LOGGER.info("Error creating new connection when starting connection pool, ignoring", e);
        }
    }

    int poolSize() {
        return pool.size();
    }

    @Override
    public void close() {

        // expecting surrounding environment to block new requests for
        // connections before calling this method. Still previously unchecked
        // connections may be returned. I.e. "pool" will not grow during
        // shutdown, which is the only thing that we need

        for (PoolAwareConnection c : pool.keySet()) {
            retire(c);
        }

        available.clear();
        pool = Collections.emptyMap();
    }

    /**
     * Closes the connection and removes it from the pool. The connection must
     * be an unchecked connection.
     */
    void retire(PoolAwareConnection connection) {
        pool.remove(connection);

        poolCap.release();

        try {
            connection.getConnection().close();
        } catch (SQLException e) {
            // ignore?
        }
    }

    /**
     * Returns connection back to the pool if possible. The connection must be
     * an unchecked connection.
     */
    void reclaim(PoolAwareConnection connection) {

        // TODO: rollback any in-process tx?

        // the queue may overflow potentially and we won't be able to add the
        // object
        if (!available.offer(connection)) {
            retire(connection);
        }
    }

    PoolAwareConnection uncheckNonBlocking(boolean validate) {
        PoolAwareConnection c = available.poll();
        return validate ? validateUnchecked(c) : c;
    }

    PoolAwareConnection uncheckBlocking(boolean validate) {
        PoolAwareConnection c;
        try {
            c = available.poll(maxQueueWaitTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null;
        }

        return validate ? validateUnchecked(c) : c;
    }

    PoolAwareConnection validateUnchecked(PoolAwareConnection c) {

        if (c == null || c.validate()) {
            return c;
        }

        // this will recursively validate all connections that exist in the pool
        // until a valid one is found or a pool is exhausted
        retire(c);
        return validateUnchecked(available.poll());
    }

    PoolAwareConnection createUnchecked() throws SQLException {

        if (!poolCap.tryAcquire()) {
            return null;
        }

        PoolAwareConnection c;
        try {
            c = createWrapped();
        } catch (SQLException e) {
            poolCap.release();
            throw e;
        }

        pool.put(c, 1);

        // even though we got a fresh connection, let's still validate it...
        // This will provide consistent behavior between cached and uncached
        // connections in respect to invalid validation queries
        if (!c.validate()) {
            throw new BadValidationQueryException(
                    "Can't validate a fresh connection. Likely validation query is wrong: " + validationQuery);
        }

        return c;
    }

    PoolAwareConnection createWrapped() throws SQLException {
        return new PoolAwareConnection(this, createUnwrapped(), validationQuery);
    }

    /**
     * Creates a new connection.
     */
    Connection createUnwrapped() throws SQLException {
        return nonPoolingDataSource.getConnection();
    }

    /**
     * Updates connection state to a default state.
     */
    Connection resetState(Connection c) throws SQLException {
        if (!c.getAutoCommit()) {
            c.setAutoCommit(true);
        }

        c.clearWarnings();
        return c;
    }

    @Override
    public Connection getConnection() throws SQLException {

        // strategy for getting a connection -
        // 1. quick peek for available connections
        // 2. create new one
        // 3. wait for a user to return connection

        PoolAwareConnection c;

        c = uncheckNonBlocking(true);
        if (c != null) {
            return resetState(c);
        }

        c = createUnchecked();
        if (c != null) {
            return resetState(c);
        }

        c = uncheckBlocking(true);
        if (c != null) {
            return resetState(c);
        }

        int poolSize = poolSize();
        int canGrow = poolCap.availablePermits();

        throw new ConnectionUnavailableException("Can't obtain connection. Request to pool timed out. Total pool size: "
                + poolSize + ", can expand by: " + canGrow);
    }

    @Override
    public Connection getConnection(String userName, String password) throws SQLException {
        throw new UnsupportedOperationException(
                "Connections for a specific user are not supported by the pooled DataSource");
    }

    @Override
    public int getLoginTimeout() throws java.sql.SQLException {
        return nonPoolingDataSource.getLoginTimeout();
    }

    @Override
    public void setLoginTimeout(int seconds) throws java.sql.SQLException {
        nonPoolingDataSource.setLoginTimeout(seconds);
    }

    @Override
    public PrintWriter getLogWriter() throws java.sql.SQLException {
        return nonPoolingDataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws java.sql.SQLException {
        nonPoolingDataSource.setLogWriter(out);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return (PoolingDataSource.class.equals(iface)) ? true : nonPoolingDataSource.isWrapperFor(iface);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return PoolingDataSource.class.equals(iface) ? (T) this : nonPoolingDataSource.unwrap(iface);
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return nonPoolingDataSource.getParentLogger();
    }

    static class BadValidationQueryException extends SQLException {
        public BadValidationQueryException(String message) {
            super(message);
        }
    }

    static class ConnectionUnavailableException extends SQLException {
        public ConnectionUnavailableException(String message) {
            super(message);
        }
    }
}

