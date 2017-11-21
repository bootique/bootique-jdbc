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
import java.security.AccessControlException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.zaxxer.hikari.util.UtilityElf.getNullIfEmpty;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@BQConfig("Pooling Hikari JDBC DataSource configuration.")
@JsonTypeName("hikari")
public class HikariCPManagedDataSourceFactory implements ManagedDataSourceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(HikariCPManagedDataSourceFactory.class);

    private static final char[] ID_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private static final long CONNECTION_TIMEOUT = SECONDS.toMillis(30);
    private static final long VALIDATION_TIMEOUT = SECONDS.toMillis(5);
    private static final long IDLE_TIMEOUT = MINUTES.toMillis(10);
    private static final long MAX_LIFETIME = MINUTES.toMillis(30);
    private static final int DEFAULT_POOL_SIZE = 10;

    private static boolean unitTest = false;

    // Properties changeable at runtime through the HikariConfigMXBean
    //
    private volatile long connectionTimeout;
    private volatile long validationTimeout;
    private volatile long idleTimeout;
    private volatile long leakDetectionThreshold;
    private volatile long maxLifetime;
    private volatile int maxPoolSize;
    private volatile int minIdle;
    private volatile String username;
    private volatile String password;

    // Properties NOT changeable at runtime
    //
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
    private boolean isAutoCommit;
    private boolean isReadOnly;
    private boolean isIsolateInternalQueries;
    private boolean isRegisterMbeans;
    private boolean isAllowPoolSuspension;
    private DataSource dataSource;
    private Properties dataSourceProperties;
    private ThreadFactory threadFactory;
    private ScheduledExecutorService scheduledExecutor;

    public HikariCPManagedDataSourceFactory() {

        dataSourceProperties = new Properties();

        minIdle = -1;
        maxPoolSize = -1;
        maxLifetime = MAX_LIFETIME;
        connectionTimeout = CONNECTION_TIMEOUT;
        validationTimeout = VALIDATION_TIMEOUT;
        idleTimeout = IDLE_TIMEOUT;
        initializationFailTimeout = 1;
        isAutoCommit = true;

        String systemProp = System.getProperty("hikaricp.configurationFile");
        if (systemProp != null) {
            loadProperties(systemProp);
        }
    }

    public HikariCPManagedDataSourceFactory(Properties properties) {
        this();
        PropertyElf.setTargetFromProperties(this, properties);
    }

    public HikariCPManagedDataSourceFactory(String propertyFileName) {
        this();
        loadProperties(propertyFileName);
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

    @BQConfigProperty
    public void setConnectionTimeout(long connectionTimeoutMs) {
        if (connectionTimeoutMs == 0L) {
            this.connectionTimeout = 2147483647L;
        } else {
            if (connectionTimeoutMs < 250L) {
                throw new IllegalArgumentException("connectionTimeout cannot be less than 250ms");
            }
            this.connectionTimeout = connectionTimeoutMs;
        }

    }

    @BQConfigProperty
    public void setIdleTimeout(long idleTimeoutMs) {
        if (idleTimeoutMs < 0L) {
            throw new IllegalArgumentException("idleTimeout cannot be negative");
        } else {
            this.idleTimeout = idleTimeoutMs;
        }
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
        } else {
            this.maxPoolSize = maxPoolSize;
        }
    }

    @BQConfigProperty
    public void setMinimumIdle(int minIdle) {
        if (minIdle < 0) {
            throw new IllegalArgumentException("minimumIdle cannot be negative");
        } else {
            this.minIdle = minIdle;
        }
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
        if (validationTimeoutMs < 250L) {
            throw new IllegalArgumentException("validationTimeout cannot be less than 250ms");
        } else {
            this.validationTimeout = validationTimeoutMs;
        }
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
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
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
                } catch (ClassNotFoundException var6) {
                    LOGGER.debug("Driver class {} not found in Thread context class loader {}, trying classloader {}", new Object[]{driverClassName, threadContextClassLoader, this.getClass().getClassLoader()});
                }
            }

            if (driverClass == null) {
                driverClass = this.getClass().getClassLoader().loadClass(driverClassName);
                LOGGER.debug("Driver class {} found in the HikariConfig class classloader {}", driverClassName, this.getClass().getClassLoader());
            }
        } catch (ClassNotFoundException var7) {
            LOGGER.error("Failed to load driver class {} from HikariConfig class classloader {}", driverClassName, this.getClass().getClassLoader());
        }

        if (driverClass == null) {
            throw new RuntimeException("Failed to load driver class " + driverClassName + " in either of HikariConfig class loader or Thread context classloader");
        } else {
            try {
                driverClass.newInstance();
                this.driverClassName = driverClassName;
            } catch (Exception var5) {
                throw new RuntimeException("Failed to instantiate class " + driverClassName, var5);
            }
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
    public void setAutoCommit(boolean isAutoCommit) {
        this.isAutoCommit = isAutoCommit;
    }

    @BQConfigProperty
    public void setAllowPoolSuspension(boolean isAllowPoolSuspension) {
        this.isAllowPoolSuspension = isAllowPoolSuspension;
    }

    @BQConfigProperty
    public void setInitializationFailTimeout(long initializationFailTimeout) {
        this.initializationFailTimeout = initializationFailTimeout;
    }

    @BQConfigProperty
    public void setIsolateInternalQueries(boolean isolate) {
        this.isIsolateInternalQueries = isolate;
    }

    @BQConfigProperty
    public void setReadOnly(boolean readOnly) {
        this.isReadOnly = readOnly;
    }

    @BQConfigProperty
    public void setRegisterMbeans(boolean register) {
        this.isRegisterMbeans = register;
    }

    @BQConfigProperty
    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    @BQConfigProperty
    public void setScheduledExecutor(ScheduledExecutorService executor) {
        this.scheduledExecutor = executor;
    }

    @BQConfigProperty
    public void setSchema(String schema) {
        this.schema = schema;
    }

    @BQConfigProperty
    public void setTransactionIsolation(String isolationLevel) {
        this.transactionIsolationName = isolationLevel;
    }

    @BQConfigProperty
    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void validate() {
        Objects.requireNonNull(jdbcUrl, "'jdbcUrl' property should not be null");

        if (poolName == null) {
            poolName = generatePoolName();
        } else if (isRegisterMbeans && poolName.contains(":")) {
            throw new IllegalArgumentException("poolName cannot contain ':' when used with JMX");
        }

        // treat empty property as null
        catalog = getNullIfEmpty(catalog);
        connectionInitSql = getNullIfEmpty(connectionInitSql);
        connectionTestQuery = getNullIfEmpty(connectionTestQuery);
        transactionIsolationName = getNullIfEmpty(transactionIsolationName);
        dataSourceClassName = getNullIfEmpty(dataSourceClassName);
        dataSourceJndiName = getNullIfEmpty(dataSourceJndiName);
        driverClassName = getNullIfEmpty(driverClassName);
        jdbcUrl = getNullIfEmpty(jdbcUrl);

        // Check Data Source Options
        if (dataSource != null) {
            if (dataSourceClassName != null) {
                LOGGER.warn("{} - using dataSource and ignoring dataSourceClassName.", poolName);
            }
        } else if (dataSourceClassName != null) {
            if (driverClassName != null) {
                LOGGER.error("{} - cannot use driverClassName and dataSourceClassName together.", poolName);
                // NOTE: This exception text is referenced by a Spring Boot FailureAnalyzer, it should not be
                // changed without first notifying the Spring Boot developers.
                throw new IllegalStateException("cannot use driverClassName and dataSourceClassName together.");
            } else if (jdbcUrl != null) {
                LOGGER.warn("{} - using dataSourceClassName and ignoring jdbcUrl.", poolName);
            }
        } else if (jdbcUrl != null || dataSourceJndiName != null) {
            // ok
        } else if (driverClassName != null) {
            LOGGER.error("{} - jdbcUrl is required with driverClassName.", poolName);
            throw new IllegalArgumentException("jdbcUrl is required with driverClassName.");
        } else {
            LOGGER.error("{} - dataSource or dataSourceClassName or jdbcUrl is required.", poolName);
            throw new IllegalArgumentException("dataSource or dataSourceClassName or jdbcUrl is required.");
        }

        validateNumerics();

        if (LOGGER.isDebugEnabled() || unitTest) {
            logConfiguration();
        }
    }

    private void validateNumerics() {
        if (maxLifetime != 0 && maxLifetime < SECONDS.toMillis(30)) {
            LOGGER.warn("{} - maxLifetime is less than 30000ms, setting to default {}ms.", poolName, MAX_LIFETIME);
            maxLifetime = MAX_LIFETIME;
        }

        if (idleTimeout + SECONDS.toMillis(1) > maxLifetime && maxLifetime > 0) {
            LOGGER.warn("{} - idleTimeout is close to or more than maxLifetime, disabling it.", poolName);
            idleTimeout = 0;
        }

        if (idleTimeout != 0 && idleTimeout < SECONDS.toMillis(10)) {
            LOGGER.warn("{} - idleTimeout is less than 10000ms, setting to default {}ms.", poolName, IDLE_TIMEOUT);
            idleTimeout = IDLE_TIMEOUT;
        }

        if (leakDetectionThreshold > 0 && !unitTest) {
            if (leakDetectionThreshold < SECONDS.toMillis(2) || (leakDetectionThreshold > maxLifetime && maxLifetime > 0)) {
                LOGGER.warn("{} - leakDetectionThreshold is less than 2000ms or more than maxLifetime, disabling it.", poolName);
                leakDetectionThreshold = 0;
            }
        }

        if (connectionTimeout < 250) {
            LOGGER.warn("{} - connectionTimeout is less than 250ms, setting to {}ms.", poolName, CONNECTION_TIMEOUT);
            connectionTimeout = CONNECTION_TIMEOUT;
        }

        if (validationTimeout < 250) {
            LOGGER.warn("{} - validationTimeout is less than 250ms, setting to {}ms.", poolName, VALIDATION_TIMEOUT);
            validationTimeout = VALIDATION_TIMEOUT;
        }

        if (maxPoolSize < 1) {
            maxPoolSize = (minIdle <= 0) ? DEFAULT_POOL_SIZE : minIdle;
        }

        if (minIdle < 0 || minIdle > maxPoolSize) {
            minIdle = maxPoolSize;
        }
    }

    private void loadProperties(String propertyFileName) {
        File propFile = new File(propertyFileName);

        try {
            InputStream is = propFile.isFile() ? new FileInputStream(propFile) : this.getClass().getResourceAsStream(propertyFileName);
            Throwable var4 = null;

            try {
                if (is == null) {
                    throw new IllegalArgumentException("Cannot find property file: " + propertyFileName);
                }

                Properties props = new Properties();
                props.load(is);
                PropertyElf.setTargetFromProperties(this, props);
            } catch (Throwable var14) {
                var4 = var14;
                throw var14;
            } finally {
                if (is != null) {
                    if (var4 != null) {
                        try {
                            is.close();
                        } catch (Throwable var13) {
                            var4.addSuppressed(var13);
                        }
                    } else {
                        is.close();
                    }
                }

            }

        } catch (IOException var16) {
            throw new RuntimeException("Failed to read property file", var16);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void logConfiguration() {
        LOGGER.debug("{} - configuration:", poolName);
        final Set<String> propertyNames = new TreeSet<>(PropertyElf.getPropertyNames(HikariConfig.class));
        for (String prop : propertyNames) {
            try {
                Object value = PropertyElf.getProperty(prop, this);
                if ("dataSourceProperties".equals(prop)) {
                    Properties dsProps = PropertyElf.copyProperties(dataSourceProperties);
                    dsProps.setProperty("password", "<masked>");
                    value = dsProps;
                }

                if ("initializationFailTimeout".equals(prop) && initializationFailTimeout == Long.MAX_VALUE) {
                    value = "infinite";
                } else if ("transactionIsolation".equals(prop) && transactionIsolationName == null) {
                    value = "default";
                } else if (prop.matches("scheduledExecutorService|threadFactory") && value == null) {
                    value = "internal";
                } else if (prop.contains("jdbcUrl") && value instanceof String) {
                    value = ((String) value).replaceAll("([?&;]password=)[^&#;]*(.*)", "$1<masked>$2");
                } else if (prop.contains("password")) {
                    value = "<masked>";
                } else if (value instanceof String) {
                    value = "\"" + value + "\""; // quote to see lead/trailing spaces is any
                } else if (value == null) {
                    value = "none";
                }
                LOGGER.debug((prop + "................................................").substring(0, 32) + value);
            } catch (Exception e) {
                // continue
            }
        }
    }

    private String generatePoolName() {
        final String prefix = "HikariPool-";
        try {
            // Pool number is global to the VM to avoid overlapping pool numbers in classloader scoped environments
            synchronized (System.getProperties()) {
                final String next = String.valueOf(Integer.getInteger("com.zaxxer.hikari.pool_number", 0) + 1);
                System.setProperty("com.zaxxer.hikari.pool_number", next);
                return prefix + next;
            }
        } catch (AccessControlException e) {
            // The SecurityManager didn't allow us to read/write system properties
            // so just generate a random pool number instead
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            final StringBuilder buf = new StringBuilder(prefix);

            for (int i = 0; i < 4; i++) {
                buf.append(ID_CHARACTERS[random.nextInt(62)]);
            }

            LOGGER.info("assigned random pool name '{}' (security manager prevented access to system properties)", buf);

            return buf.toString();
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
        hikariConfig.setAutoCommit(isAutoCommit);
        hikariConfig.setReadOnly(isReadOnly);
        hikariConfig.setIsolateInternalQueries(isIsolateInternalQueries);
        hikariConfig.setRegisterMbeans(isRegisterMbeans);
        hikariConfig.setAllowPoolSuspension(isAllowPoolSuspension);
        hikariConfig.setDataSource(dataSource);
        hikariConfig.setDataSourceProperties(dataSourceProperties);
        hikariConfig.setThreadFactory(threadFactory);
        hikariConfig.setScheduledExecutor(scheduledExecutor);

        return hikariConfig;
    }
}
