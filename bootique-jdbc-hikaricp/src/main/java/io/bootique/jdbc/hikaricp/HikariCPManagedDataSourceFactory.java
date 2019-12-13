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

package io.bootique.jdbc.hikaricp;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.di.Injector;
import io.bootique.jdbc.managed.ManagedDataSourceFactory;
import io.bootique.jdbc.managed.ManagedDataSourceStarter;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@BQConfig("Pooling Hikari JDBC DataSource configuration.")
@JsonTypeName("hikari")
public class HikariCPManagedDataSourceFactory implements ManagedDataSourceFactory {

    private static final long CONNECTION_TIMEOUT = SECONDS.toMillis(30);
    private static final long VALIDATION_TIMEOUT = SECONDS.toMillis(5);
    private static final long IDLE_TIMEOUT = MINUTES.toMillis(10);
    private static final long MAX_LIFETIME = MINUTES.toMillis(30);

    private boolean allowPoolSuspension;
    private boolean autoCommit;
    private String catalog;
    private String connectionInitSql;
    private String connectionTestQuery;
    private long connectionTimeout;
    private String dataSourceClassName;
    private String dataSourceJndiName;
    private Properties dataSourceProperties;
    private String driverClassName;
    private long idleTimeout;
    private long initializationFailTimeout;
    private boolean isolateInternalQueries;
    private String jdbcUrl;
    private long leakDetectionThreshold;
    private long maxLifetime;
    private int maximumPoolSize;
    private int minimumIdle;
    private String password;
    private String schema;
    private String transactionIsolationName;
    private boolean readOnly;
    private boolean registerMbeans;
    private String username;
    private long validationTimeout;


    public HikariCPManagedDataSourceFactory() {

        dataSourceProperties = new Properties();

        minimumIdle = 10;
        maximumPoolSize = 100;
        maxLifetime = MAX_LIFETIME;
        connectionTimeout = CONNECTION_TIMEOUT;
        validationTimeout = VALIDATION_TIMEOUT;
        idleTimeout = IDLE_TIMEOUT;
        initializationFailTimeout = 1;
        autoCommit = true;
    }

    @Override
    public ManagedDataSourceStarter create(String dataSourceName, Injector injector) {

        Supplier<DataSource> startup = () -> {
            validate();
            return new HikariDataSource(toConfiguration(dataSourceName));
        };

        Consumer<DataSource> shutdown = ds -> ((HikariDataSource) ds).close();
        return create(dataSourceName, injector, startup, shutdown);
    }

    protected ManagedDataSourceStarter create(
            String dataSourceName,
            Injector injector,
            Supplier<DataSource> startup,
            Consumer<DataSource> shutdown) {

        return new ManagedDataSourceStarter(getJdbcUrl(), startup, shutdown);
    }

    protected void validate() {
        Objects.requireNonNull(jdbcUrl, "'jdbcUrl' property should not be null");
    }

    @BQConfigProperty
    public void setConnectionTimeout(long connectionTimeoutMs) {
        if (connectionTimeoutMs == 0) {
            this.connectionTimeout = Integer.MAX_VALUE;
        } else if (connectionTimeoutMs < 250) {
            throw new IllegalArgumentException("connectionTimeout cannot be less than 250ms");
        } else {
            this.connectionTimeout = connectionTimeoutMs;
        }
    }

    @BQConfigProperty
    public void setIdleTimeout(long idleTimeoutMs) {
        if (idleTimeoutMs < 0) {
            throw new IllegalArgumentException("idleTimeout cannot be negative");
        }
        this.idleTimeout = idleTimeoutMs;
    }

    @BQConfigProperty
    public void setLeakDetectionThreshold(long leakDetectionThresholdMs) {
        this.leakDetectionThreshold = leakDetectionThresholdMs;
    }

    @BQConfigProperty
    public void setMaxLifetime(long maxLifetimeMs) {
        this.maxLifetime = maxLifetimeMs;
    }

    @BQConfigProperty
    public void setMaximumPoolSize(int maxPoolSize) {
        if (maxPoolSize < 1) {
            throw new IllegalArgumentException("'maximumPoolSize' cannot be less than 1");
        }
        this.maximumPoolSize = maxPoolSize;
    }

    @BQConfigProperty
    public void setMinimumIdle(int minIdle) {
        if (minIdle < 0) {
            throw new IllegalArgumentException("'minimumIdle' cannot be negative");
        }
        this.minimumIdle = minIdle;
    }

    @BQConfigProperty
    public void setPassword(String password) {
        this.password = password;
    }

    @BQConfigProperty
    public void setUsername(String username) {
        this.username = username;
    }

    @BQConfigProperty
    public void setValidationTimeout(long validationTimeoutMs) {
        if (validationTimeoutMs < 250) {
            throw new IllegalArgumentException("'validationTimeout' cannot be less than 250ms");
        }

        this.validationTimeout = validationTimeoutMs;
    }

    @BQConfigProperty
    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    @BQConfigProperty
    public void setConnectionTestQuery(String connectionTestQuery) {
        this.connectionTestQuery = connectionTestQuery;
    }

    @BQConfigProperty
    public void setConnectionInitSql(String connectionInitSql) {
        this.connectionInitSql = connectionInitSql;
    }

    @BQConfigProperty
    public void setDataSourceClassName(String className) {
        this.dataSourceClassName = className;
    }

    @BQConfigProperty
    public void setDataSourceJNDI(String jndiDataSource) {
        this.dataSourceJndiName = jndiDataSource;
    }

    @BQConfigProperty
    public void setDataSourceProperties(Properties dsProperties) {
        this.dataSourceProperties.putAll(dsProperties);
    }

    @BQConfigProperty
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    @BQConfigProperty
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @BQConfigProperty
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    @BQConfigProperty
    public void setAllowPoolSuspension(boolean allowPoolSuspension) {
        this.allowPoolSuspension = allowPoolSuspension;
    }

    @BQConfigProperty
    public void setInitializationFailTimeout(long initializationFailTimeout) {
        this.initializationFailTimeout = initializationFailTimeout;
    }

    @BQConfigProperty
    public void setIsolateInternalQueries(boolean isolate) {
        this.isolateInternalQueries = isolate;
    }

    @BQConfigProperty
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @BQConfigProperty
    public void setRegisterMbeans(boolean register) {
        this.registerMbeans = register;
    }

    @BQConfigProperty
    public void setSchema(String schema) {
        this.schema = schema;
    }

    @BQConfigProperty
    public void setTransactionIsolation(String isolationLevel) {
        this.transactionIsolationName = isolationLevel;
    }

    protected HikariConfig toConfiguration(String dataSourceName) {

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setPoolName(dataSourceName);

        hikariConfig.setConnectionTimeout(connectionTimeout);
        hikariConfig.setValidationTimeout(validationTimeout);
        hikariConfig.setIdleTimeout(idleTimeout);
        hikariConfig.setLeakDetectionThreshold(leakDetectionThreshold);
        hikariConfig.setMaxLifetime(maxLifetime);
        hikariConfig.setMaximumPoolSize(maximumPoolSize);
        hikariConfig.setMinimumIdle(minimumIdle);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        hikariConfig.setInitializationFailTimeout(initializationFailTimeout);
        hikariConfig.setCatalog(catalog);
        hikariConfig.setConnectionInitSql(connectionInitSql);
        hikariConfig.setConnectionTestQuery(connectionTestQuery);
        hikariConfig.setDataSourceClassName(dataSourceClassName);
        hikariConfig.setDataSourceJNDI(dataSourceJndiName);

        if (driverClassName != null) {
            hikariConfig.setDriverClassName(driverClassName);
        }

        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setSchema(schema);
        hikariConfig.setTransactionIsolation(transactionIsolationName);
        hikariConfig.setAutoCommit(autoCommit);
        hikariConfig.setReadOnly(readOnly);
        hikariConfig.setIsolateInternalQueries(isolateInternalQueries);
        hikariConfig.setRegisterMbeans(registerMbeans);
        hikariConfig.setAllowPoolSuspension(allowPoolSuspension);
        hikariConfig.setDataSourceProperties(dataSourceProperties);

        // TODO: there may be more more than one Hikari pool. Would be cool to add the pool name to the thread name
        hikariConfig.setThreadFactory(new HikariThreadFactory());

        return hikariConfig;
    }

    private static class HikariThreadFactory implements ThreadFactory {

        private AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("bootique-hikari-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }
}
