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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RowKeyTest {

    @Test
    public void hashCodeTest() {

        RowKey rk1 = new RowKey(new Object[] {"a", 1});
        RowKey rk2 = new RowKey(new Object[] {"a", 1});

        RowKey rk3 = new RowKey(new Object[] {"a", 2});

        assertEquals(rk1.hashCode(), rk2.hashCode());
        assertNotEquals(rk1.hashCode(), rk3.hashCode());
    }

    @Test
    public void equals() {

        RowKey rk1 = new RowKey(new Object[] {"a", 1});
        RowKey rk2 = new RowKey(new Object[] {"a", 1});

        RowKey rk3 = new RowKey(new Object[] {"a", 2});

        assertTrue(rk1.equals(rk2));
        assertFalse(rk1.equals(rk3));
    }
}
