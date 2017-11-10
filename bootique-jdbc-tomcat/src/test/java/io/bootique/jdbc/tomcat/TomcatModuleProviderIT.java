package io.bootique.jdbc.tomcat;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class TomcatModuleProviderIT {
    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(TomcatModuleProvider.class);
    }

    @Test
    public void testMetadata() {
        BQModuleProviderChecker.testMetadata(TomcatModuleProvider.class);
    }
}
