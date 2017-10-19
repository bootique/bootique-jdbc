package io.bootique.jdbc.tomcat;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class TomcatCPLazyDataSourceFactoryTest {

    @Test(expected = IllegalStateException.class)
    public void testCreateDataSource_NoConfig() {
        TomcatCPLazyDataSourceFactory factory = new TomcatCPLazyDataSourceFactory(new HashMap<>());
        factory.createDataSource("nosuchname");
    }

    @Test
    public void testAllNames() {
        TomcatCPLazyDataSourceFactory f1 = new TomcatCPLazyDataSourceFactory(new HashMap<>());
        assertEquals(0, f1.allNames().size());

        TomcatCPLazyDataSourceFactory f2 = new TomcatCPLazyDataSourceFactory(Collections.singletonMap("a", new TomcatCPDataSourceFactory()));
        assertEquals(1, f2.allNames().size());
        Assert.assertTrue(f2.allNames().contains("a"));
    }
}
