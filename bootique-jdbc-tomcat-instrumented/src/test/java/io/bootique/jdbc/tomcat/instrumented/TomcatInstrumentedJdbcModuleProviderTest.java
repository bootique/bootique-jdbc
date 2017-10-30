package io.bootique.jdbc.tomcat.instrumented;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TomcatInstrumentedJdbcModuleProviderTest {

	@Test
	public void testPresentInJar() {
		BQModuleProviderChecker.testPresentInJar(TomcatInstrumentedJdbcModuleProvider.class);
	}

	@Test
	public void testConfigPrefix() {
		assertEquals("jdbc", new TomcatInstrumentedJdbcModule().getConfigPrefix());
	}
}
