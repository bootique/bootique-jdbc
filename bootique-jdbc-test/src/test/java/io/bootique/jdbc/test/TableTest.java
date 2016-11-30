package io.bootique.jdbc.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TableTest {

    private DatabaseChannel mockChannel;

    @Before
    public void before() {
        mockChannel = mock(DatabaseChannel.class);
    }

    @Test
    public void testInsertColumns() {
        Table t = Table.builder(mockChannel, "t").columnNames("a", "b", "c").build();

        InsertBuilder insertBuilder = t.insertColumns("c", "a");
        Assert.assertNotNull(insertBuilder);
        List<String> names = insertBuilder.columns.stream().map(Column::getName).collect(Collectors.toList());
        assertEquals("Incorrect columns or order is not preserved", asList("c", "a"), names);
    }
}
