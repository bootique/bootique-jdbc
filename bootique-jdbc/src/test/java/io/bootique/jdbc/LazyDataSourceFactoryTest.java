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

package io.bootique.jdbc;

import io.bootique.jdbc.managed.ManagedDataSourceStarter;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class LazyDataSourceFactoryTest {

    @Test
    public void testCreateManagedDataSource_NoConfig() {
        LazyDataSourceFactory factory = new LazyDataSourceFactory(Collections.emptyMap(), Collections.emptySet());
        assertThrows(IllegalStateException.class, () -> factory.createManagedDataSource("nosuchname"));
    }

    @Test
    public void testAllNames() {
        LazyDataSourceFactory f1 = new LazyDataSourceFactory(Collections.emptyMap(), Collections.emptySet());
        assertEquals(0, f1.allNames().size());

        ManagedDataSourceStarter factory = mock(ManagedDataSourceStarter.class);

        LazyDataSourceFactory f2 = new LazyDataSourceFactory(Collections.singletonMap("a", factory), Collections.emptySet());
        assertEquals(1, f2.allNames().size());
        assertTrue(f2.allNames().contains("a"));
    }
}
