package io.bootique.jdbc.test.matcher;

import io.bootique.jdbc.test.matcher.RowKey;
import org.junit.Test;

import static org.junit.Assert.*;

public class RowKeyTest {

    @Test
    public void testHashCode() {

        RowKey rk1 = new RowKey(new Object[] {"a", 1});
        RowKey rk2 = new RowKey(new Object[] {"a", 1});

        RowKey rk3 = new RowKey(new Object[] {"a", 2});

        assertEquals(rk1.hashCode(), rk2.hashCode());
        assertNotEquals(rk1.hashCode(), rk3.hashCode());
    }

    @Test
    public void testEquals() {

        RowKey rk1 = new RowKey(new Object[] {"a", 1});
        RowKey rk2 = new RowKey(new Object[] {"a", 1});

        RowKey rk3 = new RowKey(new Object[] {"a", 2});

        assertTrue(rk1.equals(rk2));
        assertFalse(rk1.equals(rk3));
    }
}
