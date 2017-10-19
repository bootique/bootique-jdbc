package io.bootique.jdbc.tomcat;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.DataSourceFactory;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import javax.management.ObjectName;
import java.sql.Connection;
import java.util.Objects;
import java.util.Properties;

/**
 * @see DataSourceFactory#parsePoolProperties(Properties)
 * @since 0.13
 */
@BQConfig("Pooling Tomcat JDBC DataSource configuration.")
public class TomcatCPDataSourceFactory {

    private int abandonWhenPercentageFull;
    private boolean alternateUsernameAllowed;
    private boolean commitOnReturn;
    private String dataSourceJNDI;
    private Boolean defaultAutoCommit;
    private String defaultCatalog;
    private Boolean defaultReadOnly;
    private String defaultTransactionIsolation;
    private String driverClassName;
    private boolean fairQueue;
    private boolean ignoreExceptionOnPreLoad;
    private int initialSize;
    private String initSQL;
    private String jdbcInterceptors;
    private boolean jmxEnabled;
    private String jmxObjectName;
    private boolean logAbandoned;
    private boolean logValidationErrors;
    private int maxActive;
    private long maxAge;
    private int maxIdle;
    private int maxWait;
    private int minEvictableIdleTimeMillis;
    private int minIdle;
    private int numTestsPerEvictionRun;
    private String password;
    private boolean propagateInterruptState;
    private boolean removeAbandoned;
    private int removeAbandonedTimeout;
    private boolean rollbackOnReturn;
    private int suspectTimeout;
    private boolean testOnBorrow;
    private boolean testOnReturn;
    private boolean testWhileIdle;
    private boolean testOnConnect;
    private int timeBetweenEvictionRunsMillis;
    private String url;
    private String username;
    private boolean useDisposableConnectionFacade;
    private boolean useEquals;
    private boolean useLock;
    private String validationQuery;
    private int validationQueryTimeout;
    private String validatorClassName;
    private long validationInterval;

    public TomcatCPDataSourceFactory() {
        // defaults are copied from Tomcat PoolProperties.
        this.abandonWhenPercentageFull = 0;
        this.alternateUsernameAllowed = false;
        this.commitOnReturn = false;
        this.fairQueue = true;
        this.ignoreExceptionOnPreLoad = false;
        this.initialSize = 10;
        // in Tomcat the default is "true", we are intentionally using false
        this.jmxEnabled = false;
        this.logAbandoned = false;
        this.logValidationErrors = false;
        this.maxActive = 100;
        this.maxAge = 0;
        this.maxIdle = 100;
        this.maxWait = 30000;
        this.minIdle = 10;
        this.minEvictableIdleTimeMillis = 60000;
        this.numTestsPerEvictionRun = 0;
        this.propagateInterruptState = false;
        this.removeAbandoned = false;
        this.removeAbandonedTimeout = 60;
        this.rollbackOnReturn = false;
        this.suspectTimeout = 0;
        this.testOnBorrow = false;
        this.testOnConnect = false;
        this.testOnReturn = false;
        this.testWhileIdle = false;
        this.timeBetweenEvictionRunsMillis = 5000;
        this.useDisposableConnectionFacade = true;
        this.useEquals = true;
        this.useLock = false;
        this.validationInterval = 30000;
        this.validationQueryTimeout = -1;
    }

    public DataSource createDataSource() {

        validate();

        PoolConfiguration poolConfig = toConfiguration();
        DataSource dataSource = new DataSource(poolConfig);

        try {
            dataSource.createPool();
        } catch (Exception e) {
            throw new RuntimeException("Error creating DataSource", e);
        }

        return dataSource;
    }

    protected void validate() {
        Objects.requireNonNull(url, "'url' property should not be null");
    }

    public boolean isPartial() {
        // should be manually aligned with #validate to avoid downstream errors.
        return url == null;
    }

    @BQConfigProperty
    public void setAbandonWhenPercentageFull(int abandonWhenPercentageFull) {
        this.abandonWhenPercentageFull = abandonWhenPercentageFull;
    }

    @BQConfigProperty
    public void setAlternateUsernameAllowed(boolean alternateUsernameAllowed) {
        this.alternateUsernameAllowed = alternateUsernameAllowed;
    }

    @BQConfigProperty
    public void setCommitOnReturn(boolean commitOnReturn) {
        this.commitOnReturn = commitOnReturn;
    }

    @BQConfigProperty
    public void setDataSourceJNDI(String dataSourceJNDI) {
        this.dataSourceJNDI = dataSourceJNDI;
    }

    @BQConfigProperty
    public void setDefaultAutoCommit(Boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
    }

    @BQConfigProperty
    public void setDefaultCatalog(String defaultCatalog) {
        this.defaultCatalog = defaultCatalog;
    }

    @BQConfigProperty
    public void setDefaultReadOnly(Boolean defaultReadOnly) {
        this.defaultReadOnly = defaultReadOnly;
    }

    @BQConfigProperty
    public void setDefaultTransactionIsolation(String defaultTransactionIsolation) {
        this.defaultTransactionIsolation = defaultTransactionIsolation;
    }

    @BQConfigProperty
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    @BQConfigProperty
    public void setFairQueue(boolean fairQueue) {
        this.fairQueue = fairQueue;
    }

    @BQConfigProperty
    public void setIgnoreExceptionOnPreLoad(boolean ignoreExceptionOnPreLoad) {
        this.ignoreExceptionOnPreLoad = ignoreExceptionOnPreLoad;
    }

    @BQConfigProperty
    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    @BQConfigProperty
    public void setInitSQL(String initSQL) {
        this.initSQL = initSQL;
    }

    @BQConfigProperty
    public void setJdbcInterceptors(String jdbcInterceptors) {
        this.jdbcInterceptors = jdbcInterceptors;
    }

    @BQConfigProperty
    public void setJmxEnabled(boolean jmxEnabled) {
        this.jmxEnabled = jmxEnabled;
    }

    @BQConfigProperty
    public void setJmxObjectName(String jmxObjectName) {
        this.jmxObjectName = jmxObjectName;
    }

    @BQConfigProperty
    public void setLogAbandoned(boolean logAbandoned) {
        this.logAbandoned = logAbandoned;
    }

    @BQConfigProperty
    public void setLogValidationErrors(boolean logValidationErrors) {
        this.logValidationErrors = logValidationErrors;
    }

    @BQConfigProperty
    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    @BQConfigProperty
    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    @BQConfigProperty
    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    @BQConfigProperty
    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    @BQConfigProperty
    public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    @BQConfigProperty
    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    @BQConfigProperty
    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    @BQConfigProperty
    public void setPassword(String password) {
        this.password = password;
    }

    @BQConfigProperty
    public void setPropagateInterruptState(boolean propagateInterruptState) {
        this.propagateInterruptState = propagateInterruptState;
    }

    @BQConfigProperty
    public void setRemoveAbandoned(boolean removeAbandoned) {
        this.removeAbandoned = removeAbandoned;
    }

    @BQConfigProperty
    public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
        this.removeAbandonedTimeout = removeAbandonedTimeout;
    }

    @BQConfigProperty
    public void setRollbackOnReturn(boolean rollbackOnReturn) {
        this.rollbackOnReturn = rollbackOnReturn;
    }

    @BQConfigProperty
    public void setSuspectTimeout(int suspectTimeout) {
        this.suspectTimeout = suspectTimeout;
    }

    @BQConfigProperty
    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    @BQConfigProperty
    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    @BQConfigProperty
    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    @BQConfigProperty
    public void setTestOnConnect(boolean testOnConnect) {
        this.testOnConnect = testOnConnect;
    }

    @BQConfigProperty
    public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    @BQConfigProperty
    public void setUsername(String username) {
        this.username = username;
    }

    @BQConfigProperty
    public void setUseDisposableConnectionFacade(boolean useDisposableConnectionFacade) {
        this.useDisposableConnectionFacade = useDisposableConnectionFacade;
    }

    @BQConfigProperty
    public void setUseEquals(boolean useEquals) {
        this.useEquals = useEquals;
    }

    @BQConfigProperty
    public void setUseLock(boolean useLock) {
        this.useLock = useLock;
    }

    @BQConfigProperty
    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    @BQConfigProperty
    public void setValidationQueryTimeout(int validationQueryTimeout) {
        this.validationQueryTimeout = validationQueryTimeout;
    }

    @BQConfigProperty
    public void setValidatorClassName(String validatorClassName) {
        this.validatorClassName = validatorClassName;
    }

    @BQConfigProperty
    public void setValidationInterval(long validationInterval) {
        this.validationInterval = validationInterval;
    }

    @BQConfigProperty
    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    // a clone of org.apache.tomcat.jdbc.pool.DataSourceFactory#parsePoolProperties(Properties)
    protected PoolConfiguration toConfiguration() {

        PoolProperties poolProperties = new PoolProperties();

        // see PROP_DEFAULTAUTOCOMMIT
        poolProperties.setDefaultAutoCommit(defaultAutoCommit);

        // see PROP_DEFAULTREADONLY
        poolProperties.setDefaultReadOnly(defaultReadOnly);

        // see PROP_DEFAULTTRANSACTIONISOLATION
        poolProperties.setDefaultTransactionIsolation(getTransactionIsolationLevelInt());

        // see PROP_DEFAULTCATALOG
        poolProperties.setDefaultCatalog(defaultCatalog);

        // see PROP_DRIVERCLASSNAME
        poolProperties.setDriverClassName(driverClassName);

        // see PROP_MAXACTIVE
        poolProperties.setMaxActive(maxActive);

        // see PROP_MAXIDLE
        poolProperties.setMaxIdle(maxIdle);

        // see PROP_MINIDLE
        poolProperties.setMinIdle(minIdle);

        // see PROP_INITIALSIZE
        poolProperties.setInitialSize(initialSize);

        // see PROP_MAXWAIT
        poolProperties.setMaxWait(maxWait);

        // see PROP_TESTONBORROW
        poolProperties.setTestOnBorrow(testOnBorrow);

        // see PROP_TESTONRETURN
        poolProperties.setTestOnReturn(testOnReturn);

        // see PROP_TESTONCONNECT
        poolProperties.setTestOnConnect(testOnConnect);

        // see PROP_TIMEBETWEENEVICTIONRUNSMILLIS
        poolProperties.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);

        // see PROP_NUMTESTSPEREVICTIONRUN
        poolProperties.setNumTestsPerEvictionRun(numTestsPerEvictionRun);

        // see PROP_MINEVICTABLEIDLETIMEMILLIS
        poolProperties.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);

        // see PROP_TESTWHILEIDLE
        poolProperties.setTestWhileIdle(testWhileIdle);

        // see PROP_PASSWORD
        poolProperties.setPassword(password);

        // see PROP_URL
        poolProperties.setUrl(url);

        // see PROP_USERNAME
        poolProperties.setUsername(username);

        // see PROP_VALIDATIONQUERY
        poolProperties.setValidationQuery(validationQuery);

        // see PROP_VALIDATIONQUERY_TIMEOUT
        poolProperties.setValidationQueryTimeout(validationQueryTimeout);

        // see PROP_VALIDATOR_CLASS_NAME
        poolProperties.setValidatorClassName(validatorClassName);

        // see PROP_VALIDATIONINTERVAL
        poolProperties.setValidationInterval(validationInterval);

        // see PROP_REMOVEABANDONED
        poolProperties.setRemoveAbandoned(removeAbandoned);

        // see PROP_REMOVEABANDONEDTIMEOUT
        poolProperties.setRemoveAbandonedTimeout(removeAbandonedTimeout);

        // see PROP_LOGABANDONED
        poolProperties.setLogAbandoned(logAbandoned);

        // see PROP_CONNECTIONPROPERTIES
        poolProperties.setDbProperties(new Properties());
        if (poolProperties.getUsername() != null) {
            poolProperties.getDbProperties().setProperty("user", poolProperties.getUsername());
        }
        if (poolProperties.getPassword() != null) {
            poolProperties.getDbProperties().setProperty("password", poolProperties.getPassword());
        }

        // see PROP_INITSQL
        poolProperties.setInitSQL(initSQL);

        // see PROP_INTERCEPTORS
        poolProperties.setJdbcInterceptors(jdbcInterceptors);

        // see PROP_JMX_ENABLED
        poolProperties.setJmxEnabled(jmxEnabled);

        // see PROP_FAIR_QUEUE
        poolProperties.setFairQueue(fairQueue);

        // see PROP_USE_EQUALS
        poolProperties.setUseEquals(useEquals);

        // see OBJECT_NAME
        if (jmxObjectName != null) {
            poolProperties.setName(ObjectName.quote(jmxObjectName));
        }

        // see PROP_ABANDONWHENPERCENTAGEFULL
        poolProperties.setAbandonWhenPercentageFull(abandonWhenPercentageFull);

        // see PROP_MAXAGE
        poolProperties.setMaxAge(maxAge);

        // see PROP_USE_CON_LOCK
        poolProperties.setUseLock(useLock);

        // see PROP_DATASOURCE_JNDI
        poolProperties.setDataSourceJNDI(dataSourceJNDI);

        // see PROP_SUSPECT_TIMEOUT
        poolProperties.setSuspectTimeout(suspectTimeout);

        // see PROP_ALTERNATE_USERNAME_ALLOWED
        poolProperties.setAlternateUsernameAllowed(alternateUsernameAllowed);

        // see PROP_COMMITONRETURN
        poolProperties.setCommitOnReturn(commitOnReturn);

        // see PROP_ROLLBACKONRETURN
        poolProperties.setRollbackOnReturn(rollbackOnReturn);

        // see PROP_USEDISPOSABLECONNECTIONFACADE
        poolProperties.setUseDisposableConnectionFacade(useDisposableConnectionFacade);

        // see PROP_LOGVALIDATIONERRORS
        poolProperties.setLogValidationErrors(logValidationErrors);

        // see PROP_PROPAGATEINTERRUPTSTATE
        poolProperties.setPropagateInterruptState(propagateInterruptState);

        // see PROP_IGNOREEXCEPTIONONPRELOAD
        poolProperties.setIgnoreExceptionOnPreLoad(ignoreExceptionOnPreLoad);

        return poolProperties;
    }

    protected int getTransactionIsolationLevelInt() {

        if (defaultTransactionIsolation == null) {
            return DataSourceFactory.UNKNOWN_TRANSACTIONISOLATION;
        }
        if ("NONE".equalsIgnoreCase(defaultTransactionIsolation)) {
            return Connection.TRANSACTION_NONE;
        }
        if ("READ_COMMITTED".equalsIgnoreCase(defaultTransactionIsolation)) {
            return Connection.TRANSACTION_READ_COMMITTED;
        }
        if ("READ_UNCOMMITTED".equalsIgnoreCase(defaultTransactionIsolation)) {
            return Connection.TRANSACTION_READ_UNCOMMITTED;
        }
        if ("REPEATABLE_READ".equalsIgnoreCase(defaultTransactionIsolation)) {
            return Connection.TRANSACTION_REPEATABLE_READ;
        }
        if ("SERIALIZABLE".equalsIgnoreCase(defaultTransactionIsolation)) {
            return Connection.TRANSACTION_SERIALIZABLE;
        }

        try {
            return Integer.parseInt(defaultTransactionIsolation);
        } catch (NumberFormatException nfex) {
            throw new IllegalStateException("Unrecognized 'defaultTransactionIsolation': "
                    + defaultTransactionIsolation);
        }
    }

}
