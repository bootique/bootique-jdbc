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

package io.bootique.jdbc.junit5;

import io.bootique.jdbc.junit5.connector.DbConnector;
import io.bootique.jdbc.junit5.sql.ExecStatementBuilder;
import io.bootique.jdbc.junit5.sql.InsertBuilder;
import io.bootique.jdbc.junit5.dataset.TableDataSet;
import io.bootique.jdbc.junit5.matcher.TableMatcher;
import io.bootique.jdbc.junit5.metadata.DbColumnMetadata;
import io.bootique.jdbc.junit5.metadata.DbTableMetadata;
import io.bootique.jdbc.junit5.metadata.TableFQName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TableTest {

    private Table createTable() {

        ExecStatementBuilder mockExecBuilder = mock(ExecStatementBuilder.class);

        DbConnector mockConnector = mock(DbConnector.class);
        when(mockConnector.execStatement()).thenReturn(mockExecBuilder);

        DbColumnMetadata[] columns = new DbColumnMetadata[]{
                new DbColumnMetadata("a", DbColumnMetadata.NO_TYPE, false, true),
                new DbColumnMetadata("b", DbColumnMetadata.NO_TYPE, false, true),
                new DbColumnMetadata("c", DbColumnMetadata.NO_TYPE, false, true)
        };

        DbTableMetadata tableMetadata = new DbTableMetadata(new TableFQName(null, null, "t"), columns);
        return new Table(mockConnector, tableMetadata);
    }

    @Test
    public void testInsertColumns() {
        Table t = createTable();

        InsertBuilder insertBuilder = t.insertColumns("c", "a");
        assertNotNull(insertBuilder);

        List<String> names = Arrays.stream(insertBuilder.getColumns())
                .map(DbColumnMetadata::getName)
                .collect(Collectors.toList());

        assertEquals(asList("c", "a"), names, "Incorrect columns or order is not preserved");
    }

    @Test
    public void testMatcher() {
        Table t = createTable();

        TableMatcher m = t.matcher();
        assertNotNull(m);
        assertSame(t, m.getTable());
    }

    @Test
    public void testCsvDataSet() {

        Table t = createTable();

        TableDataSet ds = t.csvDataSet().columns("c,b").build();
        assertEquals(2, ds.getHeader().length);
        assertEquals(0, ds.size());
    }

}
