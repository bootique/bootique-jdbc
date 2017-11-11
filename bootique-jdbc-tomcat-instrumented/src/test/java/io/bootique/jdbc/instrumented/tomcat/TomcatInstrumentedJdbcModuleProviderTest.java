package io.bootique.jdbc.instrumented.tomcat;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class TomcatInstrumentedJdbcModuleProviderTest {

    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(TomcatInstrumentedJdbcModuleProvider.class);
    }
}
