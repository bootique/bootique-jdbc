package com.nhl.bootique.jdbc.instrumented;

import static java.util.stream.Collectors.counting;
import static org.junit.Assert.assertEquals;

import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import org.junit.Test;

import com.nhl.bootique.BQModuleProvider;

public class InstrumentedJdbcModuleProviderIT {
	@Test
	public void testPresentInJar() {
		long c = StreamSupport.stream(ServiceLoader.load(BQModuleProvider.class).spliterator(), false)
				.filter(p -> p instanceof InstrumentedJdbcModuleProvider).collect(counting());
		assertEquals("No provider found", 1, c);
	}
}
