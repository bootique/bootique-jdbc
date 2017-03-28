package io.bootique.jdbc.test.csv;

import io.bootique.jdbc.test.Column;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class RowKeyFactoryTest {

    private List<Column> columns;

    @Before
    public void before() {
        columns = asList(new Column("A"), new Column("B"), new Column("C"));
    }

    @Test
    public void testCreateKey_OneColumn() {
        RowKeyFactory factory = RowKeyFactory.create(columns, new String[]{"A"});
        RowKey key = factory.createKey(new Object[]{1, 2, 3});
        assertEquals("[1]", key.toString());
    }

    @Test
    public void testCreateKey_MultiColumn() {
        RowKeyFactory factory = RowKeyFactory.create(columns, new String[]{"A", "C"});
        RowKey key = factory.createKey(new Object[]{1, 2, 3});
        assertEquals("[1, 3]", key.toString());
    }
}
