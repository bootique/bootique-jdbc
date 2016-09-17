package io.bootique.jdbc.test.junit;

import org.junit.Assert;

import java.io.File;
import java.io.OutputStream;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DerbyDatabase extends TestDatabase {

    public static final OutputStream DEV_NULL = new OutputStream() {

        @Override
        public void write(int b) {
        }
    };

    private String location;

    public DerbyDatabase(String dataSourceName) {

        super(dataSourceName);

        // suppressing derby.log in "user.dir".
        // TODO: perhaps preserve it, but route somewhere inside "location"?
        if (System.getProperty("derby.stream.error.field") == null) {
            System.setProperty("derby.stream.error.field", DerbyDatabase.class.getName() + ".DEV_NULL");
        }

        this.location = "target/derby/" + dataSourceName;
    }

    @Override
    public void before() {
        deleteDir(new File(location));
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
