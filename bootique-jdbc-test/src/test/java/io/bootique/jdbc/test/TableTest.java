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

import io.bootique.jdbc.test.dataset.TableDataSet;
import io.bootique.jdbc.test.jdbc.ExecStatementBuilder;
import io.bootique.jdbc.test.matcher.TableMatcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TableTest {

    private DatabaseChannel mockChannel;

    @Before
    public void before() {
        ExecStatementBuilder mockExecBuilder = mock(ExecStatementBuilder.class);

        mockChannel = mock(DatabaseChannel.class);
        when(mockChannel.execStatement()).thenReturn(mockExecBuilder);
    }

    @Test
    public void testInsertColumns() {
        Table t = Table.builder(mockChannel, "t").columnNames("a", "b", "c").build();

        InsertBuilder insertBuilder = t.insertColumns("c", "a");
        Assert.assertNotNull(insertBuilder);
        List<String> names = insertBuilder.columns.stream().map(Column::getName).collect(Collectors.toList());
        assertEquals("Incorrect columns or order is not preserved", asList("c", "a"), names);
    }

    @Test
    public void testMatcher() {
        Table t = Table.builder(mockChannel, "t").columnNames("a", "b", "c").build();

        TableMatcher m = t.matcher();
        assertNotNull(m);
        assertSame(t, m.getTable());
    }

    @Test
    public void testCsvDataSet() {

        Table t = Table.builder(mockChannel, "t").columnNames("a", "b", "c").build();

        TableDataSet ds = t.csvDataSet().columns("c,b").build();
        assertEquals(2, ds.getHeader().size());
        assertEquals(0, ds.size());
    }

}
