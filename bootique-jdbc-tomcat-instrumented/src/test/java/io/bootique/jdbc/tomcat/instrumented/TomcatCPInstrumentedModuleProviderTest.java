package io.bootique.jdbc.tomcat.instrumented;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TomcatCPInstrumentedModuleProviderTest {

    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(TomcatCPInstrumentedModuleProvider.class);
    }

    @Test
    public void testConfigPrefix() {
        assertEquals("jdbc", new TomcatCPInstrumentedModule().getConfixPrefix());
    }
}
