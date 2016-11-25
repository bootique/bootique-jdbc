package io.bootique.jdbc.test;

import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class TestDataManagerTest {

    private DatabaseChannel channel = mock(DatabaseChannel.class);

    @Test
    public void testGetTable() {
        Table t1 = Table.builder(channel, "t1").build();
        Table t2 = Table.builder(channel, "t2").build();

        TestDataManager dm = new TestDataManager(t1, t2);
        assertSame(t1, dm.getTable("t1"));
        assertSame(t2, dm.getTable("t2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTable_BadName() {
        Table t1 = Table.builder(channel, "t1").build();
        Table t2 = Table.builder(channel, "t2").build();

        TestDataManager dm = new TestDataManager(t1, t2);
        dm.getTable("t3");
    }

    @Test
    public void testEmptyConstructor() throws Throwable {
        TestDataManager dm = new TestDataManager();
        dm.before();
    }
}
