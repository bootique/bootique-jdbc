package com.nhl.bootique.jdbc;

import java.util.HashMap;

import org.junit.Test;

public class LazyDataSourceFactoryTest {

	@Test(expected = IllegalStateException.class)
	public void testCreateDataSource_NoConfig() {
		LazyDataSourceFactory factory = new LazyDataSourceFactory(new HashMap<>());
		factory.createDataSource("nosuchname");
	}
}
