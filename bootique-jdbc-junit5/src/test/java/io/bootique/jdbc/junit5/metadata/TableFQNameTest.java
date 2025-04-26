package io.bootique.jdbc.junit5.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TableFQNameTest {

    @Test
    void testEquals() {
        TableFQName n1 = new TableFQName("a", "b", "c");
        assertEquals(n1, new TableFQName("a", "b", "c"));
        assertNotEquals(n1, new TableFQName("A", "b", "c"));
        assertNotEquals(n1, new TableFQName(null, "b", "c"));
    }
}
