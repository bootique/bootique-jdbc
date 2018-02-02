package io.bootique.jdbc.instrumented.hikaricp;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class HikariCPInstrumentedModuleProviderTest {

    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(HikariCPInstrumentedModuleProvider.class);
    }
}
