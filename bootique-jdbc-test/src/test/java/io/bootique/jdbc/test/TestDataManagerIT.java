package io.bootique.jdbc.test;

import io.bootique.BQRuntime;
import io.bootique.test.junit.BQTestFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore
public class TestDataManagerIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();
    private static Table T1;
    private static Table T2;

    @Rule
    public TestDataManager dataManager = new TestDataManager(true, T1, T2);

    @BeforeClass
    public static void setupDB() {
        BQRuntime testRuntime = TEST_FACTORY
                .app("--config=classpath:io/bootique/jdbc/test/TestDataManagerIT.yml")
                .autoLoadModules()
                .createRuntime();

        DatabaseChannel channel = DatabaseChannel.get(testRuntime);

        channel.execStatement().exec("CREATE TABLE \"t1\" (\"id\" INT NOT NULL PRIMARY KEY, \"name\" VARCHAR(10))");
        channel.execStatement().exec("CREATE TABLE \"t2\" (\"id\" INT NOT NULL PRIMARY KEY, \"name\" VARCHAR(10), \"t1_id\" INT)");

        T1 = channel.newTable("t1").columnNames("id", "name").build();
        T2 = channel.newTable("t2").columnNames("id", "name", "t1_id").build();
    }

    @Test
    public void test1() {

        T1.matcher().assertNoMatches();
        T2.matcher().assertNoMatches();

        T1.insert(1, "x");
        T2.insert(1, "x1", 1);

        T1.matcher().assertOneMatch();
        T2.matcher().assertOneMatch();
    }

    @Test
    public void test2() {

        T1.matcher().assertNoMatches();
        T2.matcher().assertNoMatches();

        T1.insert(2, "x");
        T2.insert(2, "x2", 2);

        T1.matcher().assertOneMatch();
        T2.matcher().assertOneMatch();
    }

}
