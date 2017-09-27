package io.bootique.jdbc.hikaricp;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import com.zaxxer.hikari.util.PropertyElf;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@BQConfig("Pooling Hikari JDBC DataSource configuration.")
public class HikariCPDataSourceFactory {

    private static final long CONNECTION_TIMEOUT;
    private static final long VALIDATION_TIMEOUT;
    private static final long IDLE_TIMEOUT;
    private static final long MAX_LIFETIME;
    private static final int DEFAULT_POOL_SIZE = 10;
    private static boolean unitTest;

    static {
        CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(30L);
        VALIDATION_TIMEOUT = TimeUnit.SECONDS.toMillis(5L);
        IDLE_TIMEOUT = TimeUnit.MINUTES.toMillis(10L);
        MAX_LIFETIME = TimeUnit.MINUTES.toMillis(30L);
        unitTest = false;
    }

    private volatile long connectionTimeout;
    private volatile long validationTimeout;
    private volatile long idleTimeout;
    private volatile long leakDetectionThreshold;
    private volatile long maxLifetime;
    private volatile int maxPoolSize;
    private volatile int minIdle;
    private long initializationFailTimeout;
    private String catalog;
    private String connectionInitSql;
    private String connectionTestQuery;
    private String dataSourceClassName;
    private String dataSourceJndiName;
    private String driverClassName;
    private String jdbcUrl;
    private String password;
    private String poolName;
    private String schema;
    private String transactionIsolationName;
    private String username;
    private boolean isAutoCommit;
    private boolean isReadOnly;
    private boolean isIsolateInternalQueries;
    private boolean isRegisterMbeans;
    private boolean isAllowPoolSuspension;
    private DataSource dataSource;
    private Properties dataSourceProperties;
    private ThreadFactory threadFactory;
    private ScheduledExecutorService scheduledExecutor;
    private MetricsTrackerFactory metricsTrackerFactory;
    private Object metricRegistry;
    private Object healthCheckRegistry;
    private Properties healthCheckProperties;

    public HikariCPDataSourceFactory() {
        this.dataSourceProperties = new Properties();
        this.healthCheckProperties = new Properties();
        this.minIdle = -1;
        this.maxPoolSize = -1;
        this.maxLifetime = MAX_LIFETIME;
        this.connectionTimeout = CONNECTION_TIMEOUT;
        this.validationTimeout = VALIDATION_TIMEOUT;
        this.idleTimeout = IDLE_TIMEOUT;
        this.initializationFailTimeout = 1L;
        this.isAutoCommit = true;
        String systemProp = System.getProperty("hikaricp.configurationFile");
        if (systemProp != null) {
            this.loadProperties(systemProp);
        }
    }

    public HikariCPDataSourceFactory(Properties properties) {
        this();
        PropertyElf.setTargetFromProperties(this, properties);
    }

    public HikariCPDataSourceFactory(String propertyFileName) {
        this();
        this.loadProperties(propertyFileName);
    }

    public HikariDataSource createDataSource() {

        validate();

        HikariConfig poolConfig = toConfiguration();
        HikariDataSource dataSource = new HikariDataSource(poolConfig);

        return dataSource;
    }

    protected void validate() {
        Objects.requireNonNull(jdbcUrl, "'url' property should not be null");
    }

    public boolean isPartial() {
        // should be manually aligned with #validate to avoid downstream errors.
        return jdbcUrl == null;
    }

    @BQConfigProperty
    public static void setUnitTest(boolean unitTest) {
        HikariCPDataSourceFactory.unitTest = unitTest;
    }

    @BQConfigProperty
    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @BQConfigProperty
    public void setValidationTimeout(long validationTimeout) {
        this.validationTimeout = validationTimeout;
    }

    @BQConfigProperty
    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @BQConfigProperty
    public void setLeakDetectionThreshold(long leakDetectionThreshold) {
        this.leakDetectionThreshold = leakDetectionThreshold;
    }

    @BQConfigProperty
    public void setMaxLifetime(long maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    @BQConfigProperty
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    @BQConfigProperty
    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    @BQConfigProperty
    public void setInitializationFailTimeout(long initializationFailTimeout) {
        this.initializationFailTimeout = initializationFailTimeout;
    }

    @BQConfigProperty
    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    @BQConfigProperty
    public void setConnectionInitSql(String connectionInitSql) {
        this.connectionInitSql = connectionInitSql;
    }

    @BQConfigProperty
    public void setConnectionTestQuery(String connectionTestQuery) {
        this.connectionTestQuery = connectionTestQuery;
    }

    @BQConfigProperty
    public void setDataSourceClassName(String dataSourceClassName) {
        this.dataSourceClassName = dataSourceClassName;
    }

    @BQConfigProperty
    public void setDataSourceJndiName(String dataSourceJndiName) {
        this.dataSourceJndiName = dataSourceJndiName;
    }

    @BQConfigProperty
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    @BQConfigProperty
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @BQConfigProperty
    public void setPassword(String password) {
        this.password = password;
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
    public void setTransactionIsolationName(String transactionIsolationName) {
        this.transactionIsolationName = transactionIsolationName;
    }

    @BQConfigProperty
    public void setUsername(String username) {
        this.username = username;
    }

    @BQConfigProperty
    public void setAutoCommit(boolean autoCommit) {
        isAutoCommit = autoCommit;
    }

    @BQConfigProperty
    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }

    @BQConfigProperty
    public void setIsolateInternalQueries(boolean isolateInternalQueries) {
        isIsolateInternalQueries = isolateInternalQueries;
    }

    @BQConfigProperty
    public void setRegisterMbeans(boolean registerMbeans) {
        isRegisterMbeans = registerMbeans;
    }

    @BQConfigProperty
    public void setAllowPoolSuspension(boolean allowPoolSuspension) {
        isAllowPoolSuspension = allowPoolSuspension;
    }

    @BQConfigProperty
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @BQConfigProperty
    public void setDataSourceProperties(Properties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    @BQConfigProperty
    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @BQConfigProperty
    public void setScheduledExecutor(ScheduledExecutorService scheduledExecutor) {
        this.scheduledExecutor = scheduledExecutor;
    }

    @BQConfigProperty
    public void setMetricsTrackerFactory(MetricsTrackerFactory metricsTrackerFactory) {
        this.metricsTrackerFactory = metricsTrackerFactory;
    }

    @BQConfigProperty
    public void setMetricRegistry(Object metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @BQConfigProperty
    public void setHealthCheckRegistry(Object healthCheckRegistry) {
        this.healthCheckRegistry = healthCheckRegistry;
    }

    @BQConfigProperty
    public void setHealthCheckProperties(Properties healthCheckProperties) {
        this.healthCheckProperties = healthCheckProperties;
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

    protected HikariConfig toConfiguration() {

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setAllowPoolSuspension(isAllowPoolSuspension);
        hikariConfig.setAutoCommit(isAutoCommit);
        hikariConfig.setCatalog(catalog);
        hikariConfig.setConnectionInitSql(connectionInitSql);
        hikariConfig.setConnectionTestQuery(connectionTestQuery);
        hikariConfig.setConnectionTimeout(connectionTimeout);
        hikariConfig.setDataSource(dataSource);
        hikariConfig.setDataSourceClassName(dataSourceClassName);
        hikariConfig.setDataSourceJNDI(dataSourceJndiName);
        hikariConfig.setDataSourceProperties(dataSourceProperties);
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setHealthCheckProperties(healthCheckProperties);
        hikariConfig.setHealthCheckRegistry(healthCheckRegistry);
        hikariConfig.setIdleTimeout(idleTimeout);
        hikariConfig.setInitializationFailTimeout(initializationFailTimeout);
        hikariConfig.setIsolateInternalQueries(isIsolateInternalQueries);
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setLeakDetectionThreshold(leakDetectionThreshold);
        hikariConfig.setMaximumPoolSize(maxPoolSize);
        hikariConfig.setMaxLifetime(maxLifetime);
        hikariConfig.setMetricRegistry(metricRegistry);
        hikariConfig.setMinimumIdle(minIdle);
        hikariConfig.setMetricsTrackerFactory(metricsTrackerFactory);
        hikariConfig.setPassword(password);
        hikariConfig.setPoolName(poolName);
        hikariConfig.setReadOnly(isReadOnly);
        hikariConfig.setRegisterMbeans(isRegisterMbeans);
        hikariConfig.setScheduledExecutor(scheduledExecutor);
        hikariConfig.setSchema(schema);
        hikariConfig.setThreadFactory(threadFactory);
        hikariConfig.setTransactionIsolation(transactionIsolationName);
        hikariConfig.setUsername(username);
        hikariConfig.setValidationTimeout(validationTimeout);

        return hikariConfig;
    }

}
