package io.bootique.jdbc.test;

import io.bootique.jdbc.test.matcher.TableMatcher;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class TestDataManagerTest {

    private DatabaseChannel channel = mock(DatabaseChannel.class);

    @Test
    public void testGetTable() {
        Table t1 = Table.builder(channel, "t1").build();
        Table t2 = Table.builder(channel, "t2").build();

        TestDataManager dm = new TestDataManager(true, t1, t2);
        assertSame(t1, dm.getTable("t1"));
        assertSame(t2, dm.getTable("t2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTable_BadName() {
        Table t1 = Table.builder(channel, "t1").build();
        Table t2 = Table.builder(channel, "t2").build();

        TestDataManager dm = new TestDataManager(true, t1, t2);
        dm.getTable("t3");
    }

    @Test
    public void testEmptyConstructor() throws Throwable {
        TestDataManager dm = new TestDataManager(true);
        dm.before();
    }

    @Test
    public void testMatcher() {
        Table t1 = Table.builder(channel, "t1").build();
        Table t2 = Table.builder(channel, "t2").build();

        TestDataManager dm = new TestDataManager(true, t1, t2);

        TableMatcher m1 = dm.matcher("t1");
        assertNotNull(m1);
        assertSame(t1, m1.getTable());

        TableMatcher m2 = dm.matcher("t2");
        assertNotNull(m2);
        assertSame(t2, m2.getTable());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMatcher_BadName() {
        Table t1 = Table.builder(channel, "t1").build();
        TestDataManager dm = new TestDataManager(true, t1);
        dm.matcher("t3");
    }
}
