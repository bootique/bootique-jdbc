package io.bootique.jdbc.hikaricp;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class HikariCPLazyDataSourceFactoryTest {

    @Test(expected = IllegalStateException.class)
    public void testCreateDataSource_NoConfig() {
        HikariCPLazyDataSourceFactory factory = new HikariCPLazyDataSourceFactory(new HashMap<>());
        factory.createDataSource("nosuchname");
    }

    @Test
    public void testAllNames() {
        HikariCPLazyDataSourceFactory f1 = new HikariCPLazyDataSourceFactory(new HashMap<>());
        assertEquals(0, f1.allNames().size());

        HikariCPLazyDataSourceFactory f2 = new HikariCPLazyDataSourceFactory(Collections.singletonMap("a", new HikariCPDataSourceFactory()));
        assertEquals(1, f2.allNames().size());
        Assert.assertTrue(f2.allNames().contains("a"));
    }
}
