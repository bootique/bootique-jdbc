package io.bootique.jdbc;

import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class LazyDataSourceFactoryTest {

    @Test(expected = IllegalStateException.class)
    public void testCreateDataSource_NoConfig() {
        LazyDataSourceFactory factory = new LazyDataSourceFactory(new HashMap<>());
        factory.createDataSource("nosuchname");
    }

    @Test
    @Ignore
    public void testAllNames() {
        LazyDataSourceFactory f1 = new LazyDataSourceFactory(new HashMap<>());
        assertEquals(0, f1.allNames().size());

//        LazyDataSourceFactory f2 = new LazyDataSourceFactory(Collections.singletonMap("a", new TomcatDataSourceFactory()));
//        assertEquals(1, f2.allNames().size());
//        Assert.assertTrue(f2.allNames().contains("a"));
    }
}
