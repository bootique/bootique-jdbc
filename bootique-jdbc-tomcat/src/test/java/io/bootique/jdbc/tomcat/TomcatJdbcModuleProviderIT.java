package io.bootique.jdbc.tomcat;

import io.bootique.BQRuntime;
import io.bootique.jdbc.JdbcModule;
import io.bootique.test.junit.BQModuleProviderChecker;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import static com.google.common.collect.ImmutableList.of;

public class TomcatJdbcModuleProviderIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(TomcatJdbcModuleProvider.class);
    }

    @Test
    public void testMetadata() {
        BQModuleProviderChecker.testMetadata(TomcatJdbcModuleProvider.class);
    }

    @Test
    public void testModuleDeclaresDependencies() {
        final BQRuntime bqRuntime = testFactory.app().module(new TomcatJdbcModuleProvider()).createRuntime();
        BQModuleProviderChecker.testModulesLoaded(bqRuntime, of(
                TomcatJdbcModule.class,
                JdbcModule.class
        ));
    }
}
