package io.bootique.jdbc.hikaricp;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.inject.Injector;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.PropertyElf;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jdbc.managed.ManagedDataSourceFactory;
import io.bootique.jdbc.managed.ManagedDataSourceSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@BQConfig("Pooling Hikari JDBC DataSource configuration.")
@JsonTypeName("hikari")
public class HikariCPManagedDataSourceFactory implements ManagedDataSourceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(HikariCPManagedDataSourceFactory.class);

    private static final long CONNECTION_TIMEOUT = SECONDS.toMillis(30);
    private static final long VALIDATION_TIMEOUT = SECONDS.toMillis(5);
    private static final long IDLE_TIMEOUT = MINUTES.toMillis(10);
    private static final long MAX_LIFETIME = MINUTES.toMillis(30);

    private volatile long connectionTimeout;
    private volatile long validationTimeout;
    private volatile long idleTimeout;
    private volatile long leakDetectionThreshold;
    private volatile long maxLifetime;
    private volatile int maxPoolSize;
    private volatile int minIdle;
    private volatile String username;
    private volatile String password;
    private long initializationFailTimeout;
    private String catalog;
    private String connectionInitSql;
    private String connectionTestQuery;
    private String dataSourceClassName;
    private String dataSourceJndiName;
    private String driverClassName;
    private String jdbcUrl;
    private String poolName;
    private String schema;
    private String transactionIsolationName;
    private boolean autoCommit;
    private boolean readOnly;
    private boolean isolateInternalQueries;
    private boolean registerMbeans;
    private boolean allowPoolSuspension;
    private Properties dataSourceProperties;

    public HikariCPManagedDataSourceFactory() {

        dataSourceProperties = new Properties();

        minIdle = -1;
        maxPoolSize = -1;
        maxLifetime = MAX_LIFETIME;
        connectionTimeout = CONNECTION_TIMEOUT;
        validationTimeout = VALIDATION_TIMEOUT;
        idleTimeout = IDLE_TIMEOUT;
        initializationFailTimeout = 1;
        autoCommit = true;
    }

    @Override
    public Optional<ManagedDataSourceSupplier> create(Injector injector) {
        if (jdbcUrl == null) {
            return Optional.empty();
        }

        Supplier<DataSource> startup = () -> {

            validate();

            HikariConfig hikariConfig = toConfiguration();
            HikariDataSource dataSource = new HikariDataSource(hikariConfig);

            return dataSource;
        };

        Consumer<DataSource> shutdown = ds -> ((HikariDataSource) ds).close();

        return Optional.of(new ManagedDataSourceSupplier(getJdbcUrl(), startup, shutdown));
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
            throw new IllegalArgumentException("maxPoolSize cannot be less than 1");
        }
        this.maxPoolSize = maxPoolSize;
    }

    @BQConfigProperty
    public void setMinimumIdle(int minIdle) {
        if (minIdle < 0) {
            throw new IllegalArgumentException("minimumIdle cannot be negative");
        }
        this.minIdle = minIdle;
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
            throw new IllegalArgumentException("validationTimeout cannot be less than 250ms");
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
        Class<?> driverClass = null;
        ClassLoader threadContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            if (threadContextClassLoader != null) {
                try {
                    driverClass = threadContextClassLoader.loadClass(driverClassName);
                    LOGGER.debug("Driver class {} found in Thread context class loader {}", driverClassName, threadContextClassLoader);
                } catch (ClassNotFoundException e) {
                    LOGGER.debug("Driver class {} not found in Thread context class loader {}, trying classloader {}",
                            driverClassName, threadContextClassLoader, this.getClass().getClassLoader());
                }
            }

            if (driverClass == null) {
                driverClass = this.getClass().getClassLoader().loadClass(driverClassName);
                LOGGER.debug("Driver class {} found in the HikariConfig class classloader {}", driverClassName, this.getClass().getClassLoader());
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error("Failed to load driver class {} from HikariConfig class classloader {}", driverClassName, this.getClass().getClassLoader());
        }

        if (driverClass == null) {
            throw new RuntimeException("Failed to load driver class " + driverClassName + " in either of HikariConfig class loader or Thread context classloader");
        }

        try {
            driverClass.newInstance();
            this.driverClassName = driverClassName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate class " + driverClassName, e);
        }
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
    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    @BQConfigProperty
    public void setSchema(String schema) {
        this.schema = schema;
    }

    @BQConfigProperty
    public void setTransactionIsolation(String isolationLevel) {
        this.transactionIsolationName = isolationLevel;
    }

    private void loadProperties(String propertyFileName) {
        final File propFile = new File(propertyFileName);
        try (final InputStream is = propFile.isFile() ? new FileInputStream(propFile) : this.getClass().getResourceAsStream(propertyFileName)) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                PropertyElf.setTargetFromProperties(this, props);
            } else {
                throw new IllegalArgumentException("Cannot find property file: " + propertyFileName);
            }
        } catch (IOException io) {
            throw new RuntimeException("Failed to read property file", io);
        }
    }

    protected HikariConfig toConfiguration() {

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setConnectionTimeout(connectionTimeout);
        hikariConfig.setValidationTimeout(validationTimeout);
        hikariConfig.setIdleTimeout(idleTimeout);
        hikariConfig.setLeakDetectionThreshold(leakDetectionThreshold);
        hikariConfig.setMaxLifetime(maxLifetime);
        hikariConfig.setMaximumPoolSize(maxPoolSize);
        hikariConfig.setMinimumIdle(minIdle);
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
        hikariConfig.setPoolName(poolName);
        hikariConfig.setSchema(schema);
        hikariConfig.setTransactionIsolation(transactionIsolationName);
        hikariConfig.setAutoCommit(autoCommit);
        hikariConfig.setReadOnly(readOnly);
        hikariConfig.setIsolateInternalQueries(isolateInternalQueries);
        hikariConfig.setRegisterMbeans(registerMbeans);
        hikariConfig.setAllowPoolSuspension(allowPoolSuspension);
        hikariConfig.setDataSourceProperties(dataSourceProperties);

        return hikariConfig;
    }
}
