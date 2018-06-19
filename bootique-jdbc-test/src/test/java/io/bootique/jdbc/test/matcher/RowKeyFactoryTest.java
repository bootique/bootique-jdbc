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

package io.bootique.jdbc.test.matcher;

import io.bootique.jdbc.test.Column;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class RowKeyFactoryTest {

    private List<Column> columns;

    @Before
    public void before() {
        columns = asList(new Column("A"), new Column("B"), new Column("C"));
    }

    @Test
    public void testCreateKey_OneColumn() {
        RowKeyFactory factory = RowKeyFactory.create(columns, new String[]{"A"});
        RowKey key = factory.createKey(new Object[]{1, 2, 3});
        assertEquals("[1]", key.toString());
    }

    @Test
    public void testCreateKey_MultiColumn() {
        RowKeyFactory factory = RowKeyFactory.create(columns, new String[]{"A", "C"});
        RowKey key = factory.createKey(new Object[]{1, 2, 3});
        assertEquals("[1, 3]", key.toString());
    }
}
