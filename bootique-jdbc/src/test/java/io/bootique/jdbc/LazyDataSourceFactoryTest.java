package io.bootique.jdbc;

import io.bootique.jdbc.managed.ManagedDataSourceStarter;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class LazyDataSourceFactoryTest {

	@Test(expected = IllegalStateException.class)
	public void testCreateManagedDataSource_NoConfig() {

		LazyDataSourceFactory factory = new LazyDataSourceFactory(Collections.emptyMap(), Collections.emptySet());
		factory.createManagedDataSource("nosuchname");
	}

	@Test
	public void testAllNames() {
		LazyDataSourceFactory f1 = new LazyDataSourceFactory(Collections.emptyMap(), Collections.emptySet());
		assertEquals(0, f1.allNames().size());

		ManagedDataSourceStarter factory = mock(ManagedDataSourceStarter.class);

		LazyDataSourceFactory f2 = new LazyDataSourceFactory(Collections.singletonMap("a", factory), Collections.emptySet());
		assertEquals(1, f2.allNames().size());
		assertTrue(f2.allNames().contains("a"));
	}
}
