package io.bootique.jdbc.hikaricp;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class HikariCPModuleProviderIT {
    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(HikariCPModuleProvider.class);
    }

    @Test
    public void testMetadata() {
        BQModuleProviderChecker.testMetadata(HikariCPModuleProvider.class);
    }
}
