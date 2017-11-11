package io.bootique.jdbc;

import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class LazyDataSourceFactoryTest {

    private Injector mockInjector;

    @Before
    public void before() {
        mockInjector = mock(Injector.class);
    }

	@Test(expected = IllegalStateException.class)
	public void testCreateDataSource_NoConfig() {

		LazyDataSourceFactory factory = new LazyDataSourceFactory(Collections.emptyMap(), Collections.emptySet(), mockInjector);
		factory.createDataSource("nosuchname");
	}

	@Test
	public void testAllNames() {
		LazyDataSourceFactory f1 = new LazyDataSourceFactory(Collections.emptyMap(), Collections.emptySet(), mockInjector);
		assertEquals(0, f1.allNames().size());

		ManagedDataSourceFactory factory = mock(ManagedDataSourceFactory.class);

		LazyDataSourceFactory f2 = new LazyDataSourceFactory(Collections.singletonMap("a", factory), Collections.emptySet(), mockInjector);
		assertEquals(1, f2.allNames().size());
		assertTrue(f2.allNames().contains("a"));
	}
}
