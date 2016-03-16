package com.nhl.bootique.jdbc;

import org.junit.Test;

import com.nhl.bootique.test.junit.BQModuleProviderChecker;

public class JdbcModuleProviderIT {
	@Test
	public void testPresentInJar() {
		BQModuleProviderChecker.testPresentInJar(JdbcModuleProvider.class);
	}
}
