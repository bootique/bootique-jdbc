package io.bootique.jdbc.test;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class JdbcTestModuleProviderTest {

    @Test
    public void testAutoLoadable() {
        BQModuleProviderChecker.testPresentInJar(JdbcTestModuleProvider.class);
    }
}
