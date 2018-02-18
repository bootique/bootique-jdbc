package io.bootique.jdbc.hikaricp;

import io.bootique.BQRuntime;
import io.bootique.jdbc.JdbcModule;
import io.bootique.test.junit.BQModuleProviderChecker;
import io.bootique.test.junit.BQRuntimeChecker;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

public class HikariCPModuleProviderIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(HikariCPModuleProvider.class);
    }

    @Test
    public void testMetadata() {
        BQModuleProviderChecker.testMetadata(HikariCPModuleProvider.class);
    }

    @Test
    public void testModuleDeclaresDependencies() {
        final BQRuntime bqRuntime = testFactory.app().module(new HikariCPModuleProvider()).createRuntime();
        BQRuntimeChecker.testModulesLoaded(bqRuntime, JdbcModule.class);
    }
}
