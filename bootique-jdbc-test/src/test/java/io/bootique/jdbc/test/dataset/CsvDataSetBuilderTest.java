package io.bootique.jdbc.test.dataset;

import io.bootique.jdbc.test.Column;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.Table;
import org.junit.Before;
import org.junit.Test;

import java.sql.Types;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class CsvDataSetBuilderTest {

    private Table table;

    @Before
    public void before() {
        DatabaseChannel mockChannel = mock(DatabaseChannel.class);
        table = Table.builder(mockChannel, "t_t")
                .columns(
                        new Column("c1", Types.VARCHAR),
                        new Column("c2", Types.INTEGER),
                        new Column("c3", Types.VARBINARY))
                .build();
    }

    @Test(expected = RuntimeException.class)
    public void testBuild_BadColumns() {
        new CsvDataSetBuilder(table).columns("a,b").build();
    }

    @Test
    public void testBuild_Empty() {
        TableDataSet ds = new CsvDataSetBuilder(table).columns("c2,c1").build();
        assertEquals(2, ds.getHeader().size());
        assertEquals(0, ds.size());
    }

    @Test
    public void testBuild() {
        TableDataSet ds = new CsvDataSetBuilder(table)
                .columns("c2,c1")
                .rows(
                        "1,z",
                        "35,\"a\""
                ).build();

        assertEquals(2, ds.getHeader().size());
        assertEquals(2, ds.size());

        assertEquals(1, ds.getRecords().get(0)[0]);
        assertEquals("z", ds.getRecords().get(0)[1]);
        assertEquals(35, ds.getRecords().get(1)[0]);
        assertEquals("a", ds.getRecords().get(1)[1]);
    }

}
