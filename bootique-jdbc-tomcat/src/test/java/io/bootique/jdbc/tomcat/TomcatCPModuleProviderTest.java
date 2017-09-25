package io.bootique.jdbc.tomcat;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class TomcatCPModuleProviderTest {
    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(TomcatCPModuleProvider.class);
    }

    @Test
    public void testMetadata() {
        BQModuleProviderChecker.testMetadata(TomcatCPModuleProvider.class);
    }
}
