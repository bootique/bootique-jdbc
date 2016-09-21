package io.bootique.jdbc.test.derby;

import io.bootique.jdbc.test.runtime.DbLifecycleListener;
import io.bootique.log.BootLogger;
import org.junit.Assert;

import java.io.File;
import java.io.OutputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.fail;

public class DerbyLifecycleListener implements DbLifecycleListener {

    public static final OutputStream DEV_NULL = new OutputStream() {
        @Override
        public void write(int b) {
        }
    };
    private static final Pattern DERBY_URL_PATTERN = Pattern.compile("^jdbc:derby:([^;:]+)");

    private BootLogger bootLogger;

    public DerbyLifecycleListener(BootLogger bootLogger) {

        this.bootLogger = bootLogger;

        // suppressing derby.log in "user.dir".
        // TODO: perhaps preserve it, but route somewhere inside "location"?
        if (System.getProperty("derby.stream.error.field") == null) {
            System.setProperty("derby.stream.error.field", DerbyLifecycleListener.class.getName() + ".DEV_NULL");
        }
    }

    protected static Optional<String> getDbDir(String jdbcUrl) {
        Matcher m = DERBY_URL_PATTERN.matcher(jdbcUrl);
        return (m.find()) ? Optional.of(m.group(1)) : Optional.empty();
    }

    @Override
    public void beforeStartup(String jdbcUrl) {

        getDbDir(jdbcUrl).ifPresent(location -> {

            bootLogger.stdout("Preparing Derby server at '" + location + "'...");

            deleteDir(new File(location));

            // Need to reload the driver if there was a previous shutdown
            // see https://db.apache.org/derby/docs/10.5/devguide/tdevdvlp20349.html
            try {
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            } catch (Exception e) {
                fail("Failed to load Derby driver");
            }
        });
    }

    @Override
    public void afterShutdown(String jdbcUrl) {

        getDbDir(jdbcUrl).ifPresent(location -> {

            bootLogger.stdout("Stopping all JVM Derby servers...");

            try {
                        DriverManager.getConnection("jdbc:derby:;shutdown=true");
                    } catch (SQLException e) {
                        // the exception is actually expected on shutdown... go figure...
                    }
                }
        );
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
