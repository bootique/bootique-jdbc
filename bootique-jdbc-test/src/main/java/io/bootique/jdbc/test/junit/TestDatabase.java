package io.bootique.jdbc.test.junit;

import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.DefaultDatabaseChannel;
import io.bootique.jdbc.test.Table;
import io.bootique.test.BQTestRuntime;
import org.junit.rules.ExternalResource;

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

    public TestDatabase(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    @Override
    protected void before() {
        channel = null;
    }

    @Override
    protected void after() {
        channel = null;
    }

    public DatabaseChannel getChannel(BQTestRuntime runtime) {
        return getChannel(runtime.getRuntime());
    }

    public DatabaseChannel getChannel(BQRuntime runtime) {

        // reusing the DataSource from the Bootique runtime. Alternatively starting our own DataSource is probably a
        // bad idea, especially with embedded databases...

        if (this.channel == null) {
            DataSourceFactory dataSourceFactory = runtime.getInstance(DataSourceFactory.class);
            this.channel = new DefaultDatabaseChannel(dataSourceFactory.forName(dataSourceName));
        }

        return this.channel;
    }

    public Table.Builder newTable(BQRuntime runtime, String name) {
        return getChannel(runtime).newTable(name);
    }
}
