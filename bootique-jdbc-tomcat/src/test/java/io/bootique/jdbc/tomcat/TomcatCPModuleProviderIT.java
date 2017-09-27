package io.bootique.jdbc.tomcat;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class TomcatCPModuleProviderIT {
    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(TomcatCPModuleProvider.class);
    }
}
