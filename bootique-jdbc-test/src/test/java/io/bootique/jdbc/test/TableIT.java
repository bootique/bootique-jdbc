package io.bootique.jdbc.test;

import io.bootique.BQRuntime;
import io.bootique.resource.ResourceFactory;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TableIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();
    private static Table T1;
    private static Table T2;

    @Rule
    public TestDataManager dataManager = new TestDataManager(true, T1, T2);

    @BeforeClass
    public static void setupDB() {
        BQRuntime runtime = TEST_FACTORY
                .app("--config=classpath:io/bootique/jdbc/test/TableIT.yml")
                .autoLoadModules()
                .createRuntime();

        DatabaseChannel channel = DatabaseChannel.get(runtime);

        channel.update("CREATE TABLE \"t1\" (\"c1\" INT, \"c2\" VARCHAR(10), \"c3\" VARCHAR(10))");
        channel.update("CREATE TABLE \"t2\" (\"c1\" INT, \"c2\" INT, \"c3\" DATE, \"c4\" TIMESTAMP)");

        T1 = channel.newTable("t1").columnNames("c1", "c2", "c3").initColumnTypesFromDBMetadata().build();
        T2 = channel.newTable("t2").columnNames("c1", "c2", "c3", "c4").initColumnTypesFromDBMetadata().build();
    }

    @Test
    public void testInsert() {
        assertEquals(0, T1.getRowCount());
        T1.insert(1, "x", "y");
        assertEquals(1, T1.getRowCount());
    }

    @Test
    public void testInsertColumns1() {
        assertEquals(0, T1.getRowCount());
        T1.insertColumns("c2").values("v1").values("v2").exec();
        assertEquals(2, T1.getRowCount());
    }

    @Test
    public void testInsertColumns_OutOfOrder() {
        assertEquals(0, T1.getRowCount());
        T1.insertColumns("c2", "c1").values("v1", 1).values("v2", 2).exec();
        assertEquals(2, T1.getRowCount());
    }

    @Test
    public void testInsertFromCsv_Empty() {
        assertEquals(0, T1.getRowCount());
        T1.insertFromCsv(new ResourceFactory("classpath:io/bootique/jdbc/test/empty.csv"));
        assertEquals(0, T1.getRowCount());
    }

    @Test
    public void testInsertFromCsv() {
        assertEquals(0, T1.getRowCount());
        T1.insertFromCsv(new ResourceFactory("classpath:io/bootique/jdbc/test/t1.csv"));

        List<Object[]> data = T1.select();
        assertEquals(2, data.size());

        // sort in memory, as there's no guarantee that DB will return data in insertion order
        data.sort(Comparator.comparing(r -> (Integer) r[0]));

        Object[] row1 = data.get(0);
        assertEquals(1, row1[0]);
        assertEquals("", row1[1]);
        assertEquals("abcd", row1[2]);

        Object[] row2 = data.get(1);
        assertEquals(2, row2[0]);
        assertEquals("tt", row2[1]);
        assertEquals("xyz", row2[2]);
    }

    @Test
    public void testContentsMatchCsv() {
        assertEquals(0, T1.getRowCount());


        T1.insertColumns("c1", "c2", "c3")
                .values(2, "tt", "xyz")
                .values(1, "", "abcd")
                .exec();

        T1.contentsMatchCsv("classpath:io/bootique/jdbc/test/t1.csv", "c1");
    }

    @Test
    public void testContentsMatchCsv_NoMatch() {
        assertEquals(0, T1.getRowCount());


        T1.insertColumns("c1", "c2", "c3")
                .values(1, "tt", "xyz")
                .values(2, "", "abcd")
                .exec();

        boolean succeeded;
        try {
            T1.contentsMatchCsv("classpath:io/bootique/jdbc/test/t1.csv", "c1");
            succeeded = true;
        } catch (AssertionError e) {
            // expected
            succeeded = false;
        }

        assertFalse("Must have failed - data sets do not match", succeeded);
    }

    @Test
    public void testContentsMatchCsv_Dates() {
        assertEquals(0, T2.getRowCount());


        T2.insertColumns("c1", "c2", "c3", "c4")
                .values(3, null, "2018-01-09", "2018-01-10 14:00:01")
                .values(1, null, "2016-01-09", "2016-01-10 10:00:00")
                .values(2, null, "2017-01-09", "2017-01-10 13:00:01")
                .exec();

        T2.contentsMatchCsv("classpath:io/bootique/jdbc/test/t2.csv", "c1");
    }

    @Test
    public void testInsertFromCsv_Nulls_Dates() {
        assertEquals(0, T2.getRowCount());
        T2.insertFromCsv(new ResourceFactory("classpath:io/bootique/jdbc/test/t2.csv"));

        List<Object[]> data = T2.select();
        assertEquals(3, data.size());

        // sort in memory, as there's no guarantee that DB will return data in insertion order
        data.sort(Comparator.comparing(r -> (Integer) r[0]));

        Object[] row1 = data.get(0);
        assertEquals(1, row1[0]);
        Assert.assertNull(row1[1]);
        assertEquals(Date.valueOf(LocalDate.of(2016, 1, 9)), row1[2]);
        assertEquals(Timestamp.valueOf(LocalDateTime.of(2016, 1, 10, 10, 0, 0)), row1[3]);

        Object[] row2 = data.get(1);
        Assert.assertNull(row2[1]);
        assertEquals(Date.valueOf(LocalDate.of(2017, 1, 9)), row2[2]);
        assertEquals(Timestamp.valueOf(LocalDateTime.of(2017, 1, 10, 13, 0, 1)), row2[3]);

        Object[] row3 = data.get(2);
        Assert.assertNull(row3[1]);
        assertEquals(Date.valueOf(LocalDate.of(2018, 1, 9)), row3[2]);
        assertEquals(Timestamp.valueOf(LocalDateTime.of(2018, 1, 10, 14, 0, 1)), row3[3]);
    }

    @Test
    public void testInsertDateTimeColumns() {
        assertEquals(0, T2.getRowCount());

        T2.insertColumns("c1", "c2", "c3", "c4")
                .values(1, null, LocalDate.parse("2018-01-09"), LocalDateTime.parse("2018-01-10T04:00:01"))
                .values(2, null, "2016-01-09", "2016-01-10 10:00:00")
                .values(3, 3, "2017-01-09", "2017-01-10 13:00:01")
                .exec();
        assertEquals(3, T2.getRowCount());
    }

    @Test
    public void testUpdate() {
        assertEquals(0, T1.getRowCount());
        T1.insert(1, "x", "y");
        T1.update()
                .set("c1", 2, Types.INTEGER)
                .set("c2", "a", Types.VARCHAR)
                .set("c3", "b", Types.VARCHAR)
                .execute();

        List<Object[]> data = T1.select();
        assertEquals(1, data.size());

        Object[] row = data.get(0);
        assertEquals(2, row[0]);
        assertEquals("a", row[1]);
        assertEquals("b", row[2]);
    }

    @Test
    public void testUpdateColumns_OutOfOrder() {
        assertEquals(0, T2.getRowCount());
        T2.insert(1, 2, LocalDate.now(), null);

        T2.update()
                .set("c3", LocalDate.parse("2018-01-09"), Types.DATE)
                .set("c1", "3", Types.INTEGER)
                .set("c2", 4, Types.INTEGER)
                .execute();

        List<Object[]> data = T2.select();
        assertEquals(1, data.size());

        Object[] row = data.get(0);
        assertEquals(3, row[0]);
        assertEquals(4, row[1]);
        assertEquals(Date.valueOf("2018-01-09"), row[2]);
        assertEquals(null, row[3]);
    }

    @Test
    public void testUpdateColumns_Where() {
        assertEquals(0, T2.getRowCount());
        T2.insert(1, 0, LocalDate.now(), new Date(0));

        T2.update()
                .set("c3", LocalDate.parse("2018-01-09"), Types.DATE)
                .set("c1", "3", Types.INTEGER)
                .set("c2", 4, Types.INTEGER)
                .set("c4", null, Types.TIMESTAMP)
                .where("c1", 1)
                .execute();

        List<Object[]> data = T2.select();
        assertEquals(1, data.size());

        Object[] row = data.get(0);
        assertEquals(3, row[0]);
        assertEquals(4, row[1]);
        assertEquals(Date.valueOf("2018-01-09"), row[2]);
        assertEquals(null, row[3]);
    }

}
