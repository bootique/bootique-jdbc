package io.bootique.jdbc.managed;

import com.google.inject.Injector;
import org.junit.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ManagedDataSourceFactoryProxyTest {

    @Test
    public void testLeafFactories() {

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
        public Optional<ManagedDataSourceSupplier> create(Injector injector) {
            return Optional.empty();
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
        public Optional<ManagedDataSourceSupplier> create(Injector injector) {
            return Optional.empty();
        }
    }

    public static class Y2 extends Y1 {
    }


    public static class Y3 extends Y2 {
    }
}
