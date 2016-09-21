package io.bootique.jdbc.test.junit;

import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.DefaultDatabaseChannel;
import io.bootique.jdbc.test.Table;
import io.bootique.log.BootLogger;
import io.bootique.test.BQTestRuntime;
import org.junit.rules.ExternalResource;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * An object that sets up a database for the unit tests and shuts it down after the tests
 * are executed. <p>
 * Instances should be annotated within the unit tests with {@link org.junit.Rule} or
 * {@link org.junit.ClassRule}. E.g.:
 * <p>
 * <pre>
 * public class MyTest {
 *
 * 	&#64;Rule
 * 	public TestDatabase db = new TestDatabase(new DerbyDatabase("target/mydb"));
 * }
 * </pre>
 *
 * @since 0.12
 */
public class TestDatabase extends ExternalResource {

    private String dataSourceName;
    private DatabaseChannel channel;
    private BootLogger logger;
    private BiConsumer<BQRuntime, DatabaseChannel> startupCallback;

    public TestDatabase(String dataSourceName) {
        this(dataSourceName, (runtime, channel) -> {
        });
    }

    public TestDatabase(String dataSourceName, BiConsumer<BQRuntime, DatabaseChannel> startupCallback) {
        this.dataSourceName = Objects.requireNonNull(dataSourceName);
        this.startupCallback = Objects.requireNonNull(startupCallback);
    }

    @Override
    protected void after() {
        if (channel != null) {
            channel.close();
            channel = null;
        }

        if (logger != null) {
            logger.stdout("Database '" + dataSourceName + "' stopped");
            logger = null;
        }
    }

    public DatabaseChannel getChannel(BQTestRuntime runtime) {
        return getChannel(runtime.getRuntime());
    }

    public DatabaseChannel getChannel(BQRuntime runtime) {

        if (this.channel == null) {
            deferredBefore(runtime);
        }

        return this.channel;
    }

    protected BootLogger getLogger() {
        return logger;
    }

    protected void deferredBefore(BQRuntime runtime) {
        this.channel = createChannel(runtime);
        this.logger = runtime.getBootLogger();
        logger.stdout("Database '" + dataSourceName + "' started");
    }

    protected DatabaseChannel createChannel(BQRuntime runtime) {

        // reusing the DataSource from Bootique runtime. Alternatively starting our own DataSource is probably a
        // bad idea, especially with embedded databases...
        DataSourceFactory dataSourceFactory = runtime.getInstance(DataSourceFactory.class);
        DatabaseChannel channel = new DefaultDatabaseChannel(dataSourceFactory.forName(dataSourceName));
        startupCallback.accept(runtime, channel);
        return channel;
    }

    public Table.Builder newTable(BQRuntime runtime, String name) {
        return getChannel(runtime).newTable(name);
    }

    public Table.Builder newTable(BQTestRuntime runtime, String name) {
        return getChannel(runtime).newTable(name);
    }

}
