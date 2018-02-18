package io.bootique.jdbc;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class JdbcModuleProviderIT {
    @Test
    public void testAutoLoadable() {
        BQModuleProviderChecker.testAutoLoadable(JdbcModuleProvider.class);
    }

    @Test
    public void testMetadata() {
        BQModuleProviderChecker.testMetadata(JdbcModuleProvider.class);
    }
}
