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

package io.bootique.jdbc.managed;

import io.bootique.di.Injector;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManagedDataSourceFactoryProxyTest {

    @Test
    public void leafFactories() {

        Set<Class<? extends ManagedDataSourceFactory>> factories = new HashSet<>(
                asList(
                        X2.class,
                        X1.class,
                        X3.class,
                        X33.class,
                        X4.class,
                        X44.class,
                        Y1.class,
                        Y2.class,
                        Y3.class
                ));

        Set<Class<? extends ManagedDataSourceFactory>> leaves = ManagedDataSourceFactoryProxy.leafFactories(factories);
        assertEquals(4, leaves.size());
        assertTrue(leaves.contains(X33.class));
        assertTrue(leaves.contains(X4.class));
        assertTrue(leaves.contains(X44.class));
        assertTrue(leaves.contains(Y3.class));
    }

    public static class X1 implements ManagedDataSourceFactory {

        @Override
        public ManagedDataSourceStarter create(String dataSourceName, Injector injector) {
            return mock(ManagedDataSourceStarter.class);
        }
    }


    public static class X2 extends X1 {
    }

    public static class X3 extends X2 {
    }

    public static class X33 extends X2 {
    }

    public static class X4 extends X3 {
    }


    public static class X44 extends X3 {
    }

    public static class Superclass0 {
    }

    public static class Y1 extends Superclass0 implements ManagedDataSourceFactory {

        @Override
        public ManagedDataSourceStarter create(String dataSourceName, Injector injector) {
            return mock(ManagedDataSourceStarter.class);
        }
    }

    public static class Y2 extends Y1 {
    }


    public static class Y3 extends Y2 {
    }
}
