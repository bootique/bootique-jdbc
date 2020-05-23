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

package io.bootique.jdbc.test.dataset;

import io.bootique.jdbc.test.Table;
import io.bootique.jdbc.test.connector.DbConnector;
import io.bootique.jdbc.test.metadata.DbColumnMetadata;
import io.bootique.jdbc.test.metadata.DbTableMetadata;
import io.bootique.jdbc.test.metadata.TableFQName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class CsvDataSetBuilderTest {

    private static Table table;

    @BeforeAll
    public static void createTable() {

        DbColumnMetadata[] columns = new DbColumnMetadata[]{
                new DbColumnMetadata("c1", Types.VARCHAR, false, true),
                new DbColumnMetadata("c2", Types.INTEGER, false, true),
                new DbColumnMetadata("c3", Types.VARBINARY, false, true)
        };
        DbTableMetadata metadata = new DbTableMetadata(new TableFQName(null, null, "t1"), columns);
        table = new Table(mock(DbConnector.class), metadata);
    }

    @Test
    public void testBuild_BadColumns() {
        assertThrows(RuntimeException.class, () -> new CsvDataSetBuilder(table).columns("a,b").build());
    }

    @Test
    public void testBuild_Empty() {
        TableDataSet ds = new CsvDataSetBuilder(table).columns("c2,c1").build();
        assertEquals(2, ds.getHeader().length);
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

        assertEquals(2, ds.getHeader().length);
        assertEquals(2, ds.size());

        assertEquals(1, ds.getRecords().get(0)[0]);
        assertEquals("z", ds.getRecords().get(0)[1]);
        assertEquals(35, ds.getRecords().get(1)[0]);
        assertEquals("a", ds.getRecords().get(1)[1]);
    }

}
