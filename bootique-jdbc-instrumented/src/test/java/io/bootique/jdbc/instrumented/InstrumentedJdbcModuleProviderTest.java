package io.bootique.jdbc.instrumented;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InstrumentedJdbcModuleProviderTest {

	@Test
	public void testPresentInJar() {
		BQModuleProviderChecker.testPresentInJar(InstrumentedJdbcModuleProvider.class);
	}

	@Test
	public void testConfigPrefix() {
		assertEquals("jdbc", new InstrumentedJdbcModule().getConfixPrefix());
	}
}
