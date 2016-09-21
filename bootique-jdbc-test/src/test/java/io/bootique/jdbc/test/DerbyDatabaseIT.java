package io.bootique.jdbc.test;

import io.bootique.jdbc.JdbcModule;
import io.bootique.jdbc.test.junit.DerbyDatabase;
import io.bootique.jdbc.test.junit.TestDatabase;
import io.bootique.test.BQTestRuntime;
import io.bootique.test.junit.BQDaemonTestFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class DerbyDatabaseIT {

    @Rule
    public BQDaemonTestFactory testFactory = new BQDaemonTestFactory();

    @Rule
    public TestDatabase db = new DerbyDatabase("DerbyDatabaseIT", "target/derby/DerbyDatabaseIT");

    private File derbyDir;
    private DatabaseChannel channel;

    @Before
    public void before() {

        this.derbyDir = new File("target/derby/DerbyDatabaseIT");

        BQTestRuntime runtime = testFactory.app("--config=classpath:io/bootique/jdbc/test/DerbyDatabaseIT.yml")
                .module(JdbcModule.class)
                .start();
        this.channel = db.getChannel(runtime);
        assertNotNull(this.channel);
    }

    @Test
    public void testDbOp1() {

        channel.update("CREATE TABLE A (ID int)");

        assertTrue(derbyDir.exists());

        Table a = channel.newTable("A").columnNames("ID").build();

        assertEquals(0, a.getRowCount());
        a.insert(5).insert(6);
        assertEquals(2, a.getRowCount());
    }

    @Test
    public void testDbOp2() {

        // second test, to ensure the DB was cleaned up...
        
        channel.update("CREATE TABLE B (ID int)");

        assertTrue(derbyDir.exists());

        Table b = channel.newTable("B").columnNames("ID").build();

        assertEquals(0, b.getRowCount());
        b.insert(5).insert(6);
        assertEquals(2, b.getRowCount());
    }
}
