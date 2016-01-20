package com.nhl.bootique.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;

import com.codahale.metrics.MetricRegistry;

public class LazyDataSourceFactoryTest {

	@Test(expected = IllegalStateException.class)
	public void testCreateDataSource_NoConfig() {
		LazyDataSourceFactory factory = new LazyDataSourceFactory(new HashMap<>(), mock(MetricRegistry.class));
		factory.createDataSource("nosuchname");
	}

	@Test
	public void testAllNames() {
		LazyDataSourceFactory f1 = new LazyDataSourceFactory(new HashMap<>(),  mock(MetricRegistry.class));
		assertEquals(0, f1.allNames().size());

		LazyDataSourceFactory f2 = new LazyDataSourceFactory(Collections.singletonMap("a", new HashMap<>()),  mock(MetricRegistry.class));
		assertEquals(1, f2.allNames().size());
		assertTrue(f2.allNames().contains("a"));
	}
}
