package io.bootique.jdbc.tomcat.instrumented;


import io.bootique.jdbc.tomcat.TomcatCPDataSourceFactory;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TomcatCPInstrumentedLazyDataSourceFactoryTest {

    @Test(expected = IllegalStateException.class)
    public void testCreateDataSource_NoConfig() {
        TomcatCPInstrumentedLazyDataSourceFactory factory = new TomcatCPInstrumentedLazyDataSourceFactory(new HashMap<>(), null);
        factory.createDataSource("nosuchname");
    }

    @Test
    public void testAllNames() {
        TomcatCPInstrumentedLazyDataSourceFactory f1 = new TomcatCPInstrumentedLazyDataSourceFactory(new HashMap<>(), null);
        assertEquals(0, f1.allNames().size());

        TomcatCPInstrumentedLazyDataSourceFactory f2 = new TomcatCPInstrumentedLazyDataSourceFactory(Collections.singletonMap("a", new TomcatCPDataSourceFactory()), null);
        assertEquals(1, f2.allNames().size());
        assertTrue(f2.allNames().contains("a"));
    }
}
