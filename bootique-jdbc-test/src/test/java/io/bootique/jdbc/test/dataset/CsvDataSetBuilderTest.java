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
    public void build_BadColumns() {
        new CsvDataSetBuilder(table).columns("a,b").build();
    }

    @Test
    public void build_Empty() {
        TableDataSet ds = new CsvDataSetBuilder(table).columns("c2,c1").build();
        assertEquals(2, ds.getHeader().size());
        assertEquals(0, ds.size());
    }

    @Test
    public void build() {
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
