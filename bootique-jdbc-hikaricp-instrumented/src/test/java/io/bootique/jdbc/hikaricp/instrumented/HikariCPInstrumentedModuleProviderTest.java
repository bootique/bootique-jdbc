package io.bootique.jdbc.hikaricp.instrumented;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HikariCPInstrumentedModuleProviderTest {

    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(HikariCPInstrumentedModuleProvider.class);
    }

    @Test
    public void testConfigPrefix() {
        assertEquals("jdbc", new HikariCPInstrumentedModule().getConfixPrefix());
    }
}
