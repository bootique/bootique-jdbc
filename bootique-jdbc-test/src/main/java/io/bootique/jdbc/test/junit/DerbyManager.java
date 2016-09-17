package io.bootique.jdbc.test.junit;

import org.junit.Assert;
import org.junit.rules.ExternalResource;

import java.io.File;
import java.io.OutputStream;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * An object that starts a new clean Derby database in the specified location, and then shuts it down after the tests
 * are executed. <p>
 * Instances should be annotated within the unit tests with {@link org.junit.Rule} or
 * {@link org.junit.ClassRule}. E.g.:
 * <p>
 * <pre>
 * public class MyTest {
 *
 * 	&#64;Rule
 * 	public DerbyManager testFactory = new DerbyManager("target/mydb");
 * }
 * </pre>
 *
 * @since 0.12
 */
public class DerbyManager extends ExternalResource {


    public static final OutputStream DEV_NULL = new OutputStream() {

        @Override
        public void write(int b) {
        }
    };

    public DerbyManager(String location) {

        // suppressing derby.log in "user.dir".
        // TODO: perhaps preserve it, but route somewhere inside "location"?
        if (System.getProperty("derby.stream.error.field") == null) {
            System.setProperty("derby.stream.error.field", DerbyManager.class.getName() + ".DEV_NULL");
        }

        deleteDir(new File(location));
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

    @Override
    protected void after() {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            // the exception is actually expected on shutdown... go figure...
        }
    }
}
