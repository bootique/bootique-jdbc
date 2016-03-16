package com.nhl.bootique.jdbc.instrumented;

import org.junit.Test;

import com.nhl.bootique.test.junit.BQModuleProviderChecker;

public class InstrumentedJdbcModuleProviderIT {

	@Test
	public void testPresentInJar() {
		BQModuleProviderChecker.testPresentInJar(InstrumentedJdbcModuleProvider.class);
	}
}
