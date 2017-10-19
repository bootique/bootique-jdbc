package io.bootique.jdbc.hikaricp.instrumented;

import io.bootique.jdbc.hikaricp.HikariCPDataSourceFactory;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HikariCPInstrumentedLazyDataSourceFactoryTest {

    @Test(expected = IllegalStateException.class)
    public void testCreateDataSource_NoConfig() {
        HikariCPInstrumentedLazyDataSourceFactory factory = new HikariCPInstrumentedLazyDataSourceFactory(new HashMap<>(), null);
        factory.createDataSource("nosuchname");
    }

    @Test
    public void testAllNames() {
        HikariCPInstrumentedLazyDataSourceFactory f1 = new HikariCPInstrumentedLazyDataSourceFactory(new HashMap<>(), null);
        assertEquals(0, f1.allNames().size());

        HikariCPInstrumentedLazyDataSourceFactory f2 = new HikariCPInstrumentedLazyDataSourceFactory(Collections.singletonMap("a", new HikariCPDataSourceFactory()), null);
        assertEquals(1, f2.allNames().size());
        assertTrue(f2.allNames().contains("a"));
    }
}
