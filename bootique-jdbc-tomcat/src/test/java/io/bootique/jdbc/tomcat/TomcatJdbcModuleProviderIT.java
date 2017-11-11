package io.bootique.jdbc.tomcat;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class TomcatJdbcModuleProviderIT {
    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(TomcatJdbcModuleProvider.class);
    }

    @Test
    public void testMetadata() {
        BQModuleProviderChecker.testMetadata(TomcatJdbcModuleProvider.class);
    }
}
