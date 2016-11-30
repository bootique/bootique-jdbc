package io.bootique.jdbc.test;

import io.bootique.test.BQTestRuntime;
import io.bootique.test.junit.BQTestFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TableIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();
    private static Table T1;

    @Rule
    public TestDataManager dataManager = new TestDataManager(true, T1);

    @BeforeClass
    public static void setupDB() {
        BQTestRuntime testRuntime = TEST_FACTORY
                .app("--config=classpath:io/bootique/jdbc/test/TableIT.yml")
                .autoLoadModules()
                .createRuntime();

        DatabaseChannel channel = DatabaseChannel.get(testRuntime);

        channel.update("CREATE TABLE \"t1\" (\"c1\" INT, \"c2\" VARCHAR(10), \"c3\" VARCHAR(10))");

        T1 = channel.newTable("t1").columnNames("c1", "c2", "c3").build();
    }

    @Test
    public void testInsert() {
        assertEquals(0, T1.getRowCount());
        T1.insert(1, "x", "y");
        assertEquals(1, T1.getRowCount());
    }

    @Test
    public void testInsertColumns() {
        assertEquals(0, T1.getRowCount());
        T1.insertColumns("c2").values("v1").values("v2").exec();
        assertEquals(2, T1.getRowCount());
    }
}
