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

package io.bootique.jdbc.junit5.matcher;

import io.bootique.jdbc.junit5.metadata.DbColumnMetadata;
import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RowKeyFactoryTest {

    private static final DbColumnMetadata[] columns = new DbColumnMetadata[]{
            new DbColumnMetadata("A", Types.BINARY, false, true),
            new DbColumnMetadata("B", Types.BINARY, false, true),
            new DbColumnMetadata("C", Types.BINARY, false, true)
    };

    @Test
    public void createKey_OneColumn() {
        RowKeyFactory factory = RowKeyFactory.create(columns, new String[]{"A"});
        RowKey key = factory.createKey(new Object[]{1, 2, 3});
        assertEquals("[1]", key.toString());
    }

    @Test
    public void createKey_MultiColumn() {
        RowKeyFactory factory = RowKeyFactory.create(columns, new String[]{"A", "C"});
        RowKey key = factory.createKey(new Object[]{1, 2, 3});
        assertEquals("[1, 3]", key.toString());
    }
}
