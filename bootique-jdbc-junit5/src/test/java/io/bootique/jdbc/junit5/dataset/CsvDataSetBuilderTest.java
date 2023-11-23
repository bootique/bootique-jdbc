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

package io.bootique.jdbc.junit5.dataset;

import io.bootique.jdbc.junit5.Table;
import io.bootique.jdbc.junit5.connector.DbConnector;
import io.bootique.jdbc.junit5.metadata.DbColumnMetadata;
import io.bootique.jdbc.junit5.metadata.DbTableMetadata;
import io.bootique.jdbc.junit5.metadata.TableFQName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class CsvDataSetBuilderTest {

    private static Table table;

    @BeforeAll
    public static void createTable() {

        DbColumnMetadata[] columns = new DbColumnMetadata[]{
                new DbColumnMetadata("c1", Types.VARCHAR, false, true),
                new DbColumnMetadata("c2", Types.INTEGER, false, true),
                new DbColumnMetadata("c3", Types.VARBINARY, false, true),
                new DbColumnMetadata("c4", Types.BIGINT, false, true),
                new DbColumnMetadata("c5", Types.DECIMAL, false, true)
        };
        DbTableMetadata metadata = new DbTableMetadata(new TableFQName(null, null, "t1"), columns);
        table = new Table(mock(DbConnector.class), metadata);
    }

    @Test
    public void build_BadColumns() {
        assertThrows(RuntimeException.class, () -> new CsvDataSetBuilder(table).columns("a,b").build());
    }

    @Test
    public void build_Empty() {
        TableDataSet ds = new CsvDataSetBuilder(table).columns("c2,c1").build();
        assertEquals(2, ds.getHeader().length);
        assertEquals(0, ds.size());
    }

    @Test
    public void build() {
        TableDataSet ds = new CsvDataSetBuilder(table)
                .columns("c2,c1,c5,c4")
                .rows(
                        "1,z,2.345,123456789",
                        "35,\"a\",,"
                ).build();

        assertEquals(4, ds.getHeader().length);
        assertEquals(
                asList("c2", "c1", "c5", "c4"),
                stream(ds.getHeader()).map(DbColumnMetadata::getName).collect(Collectors.toList()));

        assertEquals(2, ds.size());

        assertEquals(1, ds.getRecords().get(0)[0]);
        assertEquals("z", ds.getRecords().get(0)[1]);
        assertEquals(new BigDecimal("2.345"), ds.getRecords().get(0)[2]);
        assertEquals(123456789L, ds.getRecords().get(0)[3]);

        assertEquals(35, ds.getRecords().get(1)[0]);
        assertEquals("a", ds.getRecords().get(1)[1]);
        assertNull(ds.getRecords().get(1)[2]);
        assertNull(ds.getRecords().get(1)[3]);
    }

}
