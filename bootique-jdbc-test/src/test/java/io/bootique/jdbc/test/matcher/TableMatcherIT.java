package io.bootique.jdbc.test.matcher;

import io.bootique.BQRuntime;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.Table;
import io.bootique.jdbc.test.TestDataManager;
import io.bootique.test.junit.BQTestFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.fail;

public class TableMatcherIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();
    private static Table T1;

    @Rule
    public TestDataManager dataManager = new TestDataManager(true, T1);

    @BeforeClass
    public static void setupDB() {
        BQRuntime runtime = TEST_FACTORY
                .app("-c", "classpath:io/bootique/jdbc/test/matcher/TableMatcherIT.yml")
                .autoLoadModules()
                .createRuntime();

        DatabaseChannel channel = DatabaseChannel.get(runtime);

        channel.update("CREATE TABLE \"t1\" (\"c1\" INT, \"c2\" VARCHAR(10), \"c3\" VARCHAR(10))");

        T1 = channel.newTable("t1").columnNames("c1", "c2", "c3").initColumnTypesFromDBMetadata().build();
    }

    @Test
    public void testAssertHasRows() {
        TableMatcher matcher = new TableMatcher(T1);
        matcher.assertHasRows(0);

        T1.insert(1, "y", "z");
        matcher.assertHasRows(1);

        T1.insert(2, "a", "b");
        matcher.assertHasRows(2);
    }

    @Test
    public void testAssertHasRows_WithCondition() {
        TableMatcher matcher = new TableMatcher(T1);

        T1.insert(1, "y", "z");
        T1.insert(2, "a", "b");
        matcher.eq("c2", "z").eq("c1", 1).assertHasRows(1);
    }

    @Test
    public void testAssertHasRows_Negative() {
        TableMatcher matcher = new TableMatcher(T1);
        assertAssertionError(() -> matcher.assertHasRows(1), "The matcher incorrectly assumed there's 1 row in " +
                "DB when there's none.");
    }

    @Test
    public void testAssertHasRows_WithCondition_Negative() {
        TableMatcher matcher = new TableMatcher(T1);

        T1.insert(1, "y", "z");

        assertAssertionError(() -> matcher.eq("c2", "z").eq("c1", 2).assertHasRows(1),
                "The matcher incorrectly assumed there was 1 row matching condition.");
    }

    private void assertAssertionError(Runnable test, String whenNoErrorMessage) {
        try {
            test.run();
        } catch (AssertionError e) {
            // expected...
            return;
        }

        fail("Unexpected assertion success: " + whenNoErrorMessage);
    }
}
