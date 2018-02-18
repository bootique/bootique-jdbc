package io.bootique.jdbc.test;

import io.bootique.BQRuntime;
import io.bootique.jdbc.JdbcModule;
import io.bootique.test.junit.BQModuleProviderChecker;
import io.bootique.test.junit.BQRuntimeChecker;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

public class JdbcTestModuleProviderTest {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testAutoLoadable() {
        BQModuleProviderChecker.testPresentInJar(JdbcTestModuleProvider.class);
    }

    @Test
    public void testModuleDeclaresDependencies() {
        final BQRuntime bqRuntime = testFactory.app().module(new JdbcTestModuleProvider()).createRuntime();
        BQRuntimeChecker.testModulesLoaded(bqRuntime, JdbcModule.class);
    }
}
