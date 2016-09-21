package io.bootique.jdbc.test.junit;

import io.bootique.BQRuntime;
import io.bootique.jdbc.test.DatabaseChannel;
import org.junit.Assert;

import java.io.File;
import java.io.OutputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.BiConsumer;

import static org.junit.Assert.fail;

public class DerbyDatabase extends TestDatabase {

    public static final OutputStream DEV_NULL = new OutputStream() {

        @Override
        public void write(int b) {
        }
    };

    private String location;

    public DerbyDatabase(String dataSourceName, String location) {
        this(dataSourceName, location, (runtime, channel) -> {
        });
    }

    public DerbyDatabase(String dataSourceName, String location, BiConsumer<BQRuntime, DatabaseChannel> startupCallback) {
        super(dataSourceName, startupCallback);

        // suppressing derby.log in "user.dir".
        // TODO: perhaps preserve it, but route somewhere inside "location"?
        if (System.getProperty("derby.stream.error.field") == null) {
            System.setProperty("derby.stream.error.field", DerbyDatabase.class.getName() + ".DEV_NULL");
        }

        // TODO: would be nice to extract location automatically from DataSourceFactory by parsing Derby URL
        // There's no such API there yet...
        this.location = location;
    }

    @Override
    protected void deferredBefore(BQRuntime runtime) {

        // cleanup before the call to super, as super will need to start fresh...
        deleteDir(new File(location));

        // Need to reload the driver if there was a previous shutdown
        // see https://db.apache.org/derby/docs/10.5/devguide/tdevdvlp20349.html
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        } catch (Exception e) {
            fail("Failed to load Derby driver");
        }

        super.deferredBefore(runtime);
    }

    @Override
    public void after() {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            // the exception is actually expected on shutdown... go figure...
        }

        super.after();
    }

    protected void deleteDir(File dir) {
        if (dir.exists()) {

            for (File f : dir.listFiles()) {
                if (f.isFile()) {
                    Assert.assertTrue(f.delete());
                } else {
                    deleteDir(f);
                }
            }

            Assert.assertTrue(dir.delete());
        }
    }
}
