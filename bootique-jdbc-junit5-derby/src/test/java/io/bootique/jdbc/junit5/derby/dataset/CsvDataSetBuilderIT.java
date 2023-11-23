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

package io.bootique.jdbc.junit5.derby.dataset;

import io.bootique.jdbc.junit5.Table;
import io.bootique.jdbc.junit5.derby.DerbyTester;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class CsvDataSetBuilderIT {

    @BQTestTool
    static final DerbyTester db = DerbyTester
            .db()
            .deleteBeforeEachTest("t1", "t2", "t3", "t4");

    private static Table T1;
    private static Table T2;
    private static Table T3;
    private static Table T4;

    @BeforeAll
    public static void setupDB() {

        db.execStatement().exec("CREATE TABLE \"t1\" (\"c1\" INT, \"c2\" VARCHAR(10), \"c3\" VARCHAR(10))");
        db.execStatement().exec("CREATE TABLE \"t2\" (\"c1\" INT, \"c2\" INT, \"c3\" DATE, \"c4\" TIMESTAMP)");
        db.execStatement().exec("CREATE TABLE \"t3\" (\"c1\" INT, \"c2\" VARCHAR (10) FOR BIT DATA)");
        db.execStatement().exec("CREATE TABLE \"t4\" (\"c1\" INT, \"c2\" BOOLEAN)");

        T1 = db.getTable("t1");
        T2 = db.getTable("t2");
        T3 = db.getTable("t3");
        T4 = db.getTable("t4");
    }

    @Test
    public void load_Empty() {
        T1.csvDataSet().load("classpath:io/bootique/jdbc/junit5/derby/dataset/empty.csv").persist();
        T1.matcher().assertNoMatches();
    }

    @Test
    public void csvDataSet_Load() {
        T1.csvDataSet().load("classpath:io/bootique/jdbc/junit5/derby/dataset/t1.csv").persist();

        List<Object[]> data = T1.selectAllColumns().select();
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
    public void csvDataSet_Load_Nulls_Dates() {
        T2.csvDataSet().load("classpath:io/bootique/jdbc/junit5/derby/dataset/t2.csv").persist();

        List<Object[]> data = T2.selectAllColumns().select();
        assertEquals(3, data.size());

        // sort in memory, as there's no guarantee that DB will return data in insertion order
        data.sort(Comparator.comparing(r -> (Integer) r[0]));

        Object[] row1 = data.get(0);
        assertEquals(1, row1[0]);
        assertNull(row1[1]);
        assertEquals(Date.valueOf(LocalDate.of(2016, 1, 9)), row1[2]);
        assertEquals(Timestamp.valueOf(LocalDateTime.of(2016, 1, 10, 10, 0, 0)), row1[3]);

        Object[] row2 = data.get(1);
        assertNull(row2[1]);
        assertEquals(Date.valueOf(LocalDate.of(2017, 1, 9)), row2[2]);
        assertEquals(Timestamp.valueOf(LocalDateTime.of(2017, 1, 10, 13, 0, 1)), row2[3]);

        Object[] row3 = data.get(2);
        assertNull(row3[1]);
        assertEquals(Date.valueOf(LocalDate.of(2018, 1, 9)), row3[2]);
        assertEquals(Timestamp.valueOf(LocalDateTime.of(2018, 1, 10, 14, 0, 1)), row3[3]);
    }

    @Test
    public void csvDataSet_Load_Binary() {
        T3.csvDataSet().load("classpath:io/bootique/jdbc/junit5/derby/dataset/t3.csv").persist();

        List<Object[]> data = T3.selectAllColumns().select();
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
    public void csvDataSet_Load_Boolean() {
        T4.csvDataSet().load("classpath:io/bootique/jdbc/junit5/derby/dataset/t4.csv").persist();

        List<Object[]> data = T4.selectAllColumns().select();
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

}
