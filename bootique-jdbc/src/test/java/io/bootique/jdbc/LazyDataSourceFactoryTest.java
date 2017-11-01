package io.bootique.jdbc;

import org.junit.Test;

import java.util.HashMap;

public class LazyDataSourceFactoryTest {

    @Test(expected = IllegalStateException.class)
    public void testCreateDataSource_NoConfig() {
        LazyDataSourceFactory factory = new LazyDataSourceFactory(new HashMap<>());
        factory.createDataSource("nosuchname");
    }
}
