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

import static org.junit.Assert.*;

public class TableIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();
    private static Table T1;
    private static Table T2;
    private static Table T3;
    private static Table T4;

    @Rule
    public TestDataManager dataManager = new TestDataManager(true, T1, T2, T3, T4);

    @BeforeClass
    public static void setupDB() {
        BQRuntime runtime = TEST_FACTORY
                .app("--config=classpath:io/bootique/jdbc/test/TableIT.yml")
                .autoLoadModules()
                .createRuntime();

        DatabaseChannel channel = DatabaseChannel.get(runtime);

        channel.execStatement().exec("CREATE TABLE \"t1\" (\"c1\" INT, \"c2\" VARCHAR(10), \"c3\" VARCHAR(10))");
        channel.execStatement().exec("CREATE TABLE \"t2\" (\"c1\" INT, \"c2\" INT, \"c3\" DATE, \"c4\" TIMESTAMP)");
        channel.execStatement().exec("CREATE TABLE \"t3\" (\"c1\" INT, \"c2\" VARCHAR (10) FOR BIT DATA)");
        channel.execStatement().exec("CREATE TABLE \"t4\" (\"c1\" INT, \"c2\" BOOLEAN)");

        T1 = channel.newTable("t1").columnNames("c1", "c2", "c3").initColumnTypesFromDBMetadata().build();
        T2 = channel.newTable("t2").columnNames("c1", "c2", "c3", "c4").initColumnTypesFromDBMetadata().build();
        T3 = channel.newTable("t3").columnNames("c1", "c2").initColumnTypesFromDBMetadata().build();
        T4 = channel.newTable("t4").columnNames("c1", "c2").initColumnTypesFromDBMetadata().build();
    }

    @Test
    public void testInsert() {
        T1.insert(1, "x", "y");
        T1.matcher().assertMatches(1);
    }

    @Test
    public void testInsertColumns1() {
        T1.insertColumns("c2").values("v1").values("v2").exec();
        T1.matcher().assertMatches(2);
    }

    @Test
    public void testInsertColumns_OutOfOrder() {
        T1.insertColumns("c2", "c1").values("v1", 1).values("v2", 2).exec();
        T1.matcher().assertMatches(2);
    }

    @Test
    public void testInsertFromCsv_Empty() {
        T1.insertFromCsv(new ResourceFactory("classpath:io/bootique/jdbc/test/empty.csv"));
        T1.matcher().assertNoMatches();
    }

    @Test
    public void testInsertFromCsv() {
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
    public void testInsertFromCsv_Binary() {
        T3.insertFromCsv(new ResourceFactory("classpath:io/bootique/jdbc/test/t3.csv"));

        List<Object[]> data = T3.select();
        assertEquals(3, data.size());

        // sort in memory, as there's no guarantee that DB will return data in insertion order
        data.sort(Comparator.comparing(r -> (Integer) r[0]));

        Object[] row1 = data.get(0);
        assertEquals(1, row1[0]);
        assertArrayEquals("abcd".getBytes(), (byte[]) row1[1]);

        Object[] row2 = data.get(1);
        assertEquals(2, row2[0]);
        assertArrayEquals("kmln".getBytes(), (byte[]) row2[1]);

        Object[] row3 = data.get(2);
        assertEquals(3, row3[0]);
        assertNull(row3[1]);
    }

    @Test
    public void testInsertFromCsv_Boolean() {
        T4.insertFromCsv(new ResourceFactory("classpath:io/bootique/jdbc/test/t4.csv"));

        List<Object[]> data = T4.select();
        assertEquals(3, data.size());

        // sort in memory, as there's no guarantee that DB will return data in insertion order
        data.sort(Comparator.comparing(r -> (Integer) r[0]));

        Object[] row1 = data.get(0);
        assertEquals(1, row1[0]);
        assertEquals(true, row1[1]);

        Object[] row2 = data.get(1);
        assertEquals(2, row2[0]);
        assertEquals(false, row2[1]);

        Object[] row3 = data.get(2);
        assertEquals(3, row3[0]);
        assertNull(row3[1]);
    }


    @Test
    @Deprecated
    public void testContentsMatchCsv() {

        T1.insertColumns("c1", "c2", "c3")
                .values(2, "tt", "xyz")
                .values(1, "", "abcd")
                .exec();

        T1.contentsMatchCsv("classpath:io/bootique/jdbc/test/t1.csv", "c1");
    }

    @Test
    @Deprecated
    public void testContentsMatchCsv_NoMatch() {

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
    @Deprecated
    public void testContentsMatchCsv_Dates() {

        T2.insertColumns("c1", "c2", "c3", "c4")
                .values(3, null, "2018-01-09", "2018-01-10 14:00:01")
                .values(1, null, "2016-01-09", "2016-01-10 10:00:00")
                .values(2, null, "2017-01-09", "2017-01-10 13:00:01")
                .exec();

        T2.contentsMatchCsv("classpath:io/bootique/jdbc/test/t2.csv", "c1");
    }

    @Test
    @Deprecated
    public void testContentsMatchCsv_Binary() {

        T3.insertColumns("c1", "c2")
                .values(3, null)
                .values(1, "abcd".getBytes())
                .values(2, "kmln".getBytes())
                .exec();

        T3.contentsMatchCsv("classpath:io/bootique/jdbc/test/t3.csv", "c1");
    }

    @Test
    public void testInsertFromCsv_Nulls_Dates() {
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
        T2.insertColumns("c1", "c2", "c3", "c4")
                .values(1, null, LocalDate.parse("2018-01-09"), LocalDateTime.parse("2018-01-10T04:00:01"))
                .values(2, null, "2016-01-09", "2016-01-10 10:00:00")
                .values(3, 3, "2017-01-09", "2017-01-10 13:00:01")
                .exec();
        assertEquals(3, T2.getRowCount());
    }

    @Test
    public void testUpdate() {
        T1.insert(1, "x", "y");
        T1.update()
                .set("c1", 2, Types.INTEGER)
                .set("c2", "a", Types.VARCHAR)
                .set("c3", "b", Types.VARCHAR)
                .exec();

        List<Object[]> data = T1.select();
        assertEquals(1, data.size());

        Object[] row = data.get(0);
        assertEquals(2, row[0]);
        assertEquals("a", row[1]);
        assertEquals("b", row[2]);
    }

    @Test
    public void testUpdateColumns_OutOfOrder() {
        T2.insert(1, 2, LocalDate.now(), null);

        T2.update()
                .set("c3", LocalDate.parse("2018-01-09"), Types.DATE)
                .set("c1", "3", Types.INTEGER)
                .set("c2", 4, Types.INTEGER)
                .exec();

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
        T2.insert(1, 0, LocalDate.now(), new Date(0));

        T2.update()
                .set("c3", LocalDate.parse("2018-01-09"), Types.DATE)
                .set("c1", "3", Types.INTEGER)
                .set("c2", 4, Types.INTEGER)
                .set("c4", null, Types.TIMESTAMP)
                .where("c1", 1)
                .exec();

        List<Object[]> data = T2.select();
        assertEquals(1, data.size());

        Object[] row = data.get(0);
        assertEquals(3, row[0]);
        assertEquals(4, row[1]);
        assertEquals(Date.valueOf("2018-01-09"), row[2]);
        assertEquals(null, row[3]);
    }

    @Test
    public void testGetString() {
        assertNull(T1.getString("c2"));
        T1.insert(1, "xr", "yr");
        assertEquals("xr", T1.getString("c2"));
    }

    @Test
    public void testGetInt() {
        assertEquals(0, T1.getInt("c1"));
        T1.insert(56, "xr", "yr");
        assertEquals(56, T1.getInt("c1"));
    }

}
