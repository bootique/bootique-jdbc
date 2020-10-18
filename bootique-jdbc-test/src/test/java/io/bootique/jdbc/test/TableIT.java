/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.jdbc.test;

import io.bootique.BQRuntime;
import io.bootique.test.junit.BQTestFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

public class TableIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();
    private static Table T1;
    private static Table T2;
    private static Table T3;

    @Rule
    public TestDataManager dataManager = new TestDataManager(true, T1, T2, T3);

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

        T1 = channel.newTable("t1").columnNames("c1", "c2", "c3").initColumnTypesFromDBMetadata().build();
        T2 = channel.newTable("t2").columnNames("c1", "c2", "c3", "c4").initColumnTypesFromDBMetadata().build();
        T3 = channel.newTable("t3").columnNames("c1", "c2").initColumnTypesFromDBMetadata().build();
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
    public void testInsertDateTimeColumns() {
        T2.insertColumns("c1", "c2", "c3", "c4")
                .values(1, null, LocalDate.parse("2018-01-09"), LocalDateTime.parse("2018-01-10T04:00:01"))
                .values(2, null, "2016-01-09", "2016-01-10 10:00:00")
                .values(3, 3, "2017-01-09", "2017-01-10 13:00:01")
                .exec();
        T2.matcher().assertMatches(3);
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

    @Test
    public void testDelete() {
        T1.insertColumns("c1", "c2", "c3").values(1, "a", "b").values(2, "c", "d").exec();
        T1.matcher().assertMatches(2);

        T1.delete().where("c2", "c").exec();
        T1.matcher().assertOneMatch();
        T1.matcher().eq("c1", 1).assertOneMatch();
    }

    @Test
    public void testSelectColumns() {
        T1.insertColumns("c1", "c2", "c3").values(1, "a", "b").values(2, "c", "d").exec();
        DTO dto = T1.selectColumns("c2", "c3")
                .converter(a -> new DTO((String) a[0], (String) a[1]))
                .where("c1", 2)
                .selectOne(null);

        assertEquals("c", dto.c2);
        assertEquals("d", dto.c3);
    }

    private static class DTO {
        final String c2;
        final String c3;

        public DTO(String c2, String c3) {
            this.c2 = c2;
            this.c3 = c3;
        }
    }

}
