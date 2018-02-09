package io.bootique.jdbc.instrumented;

import io.bootique.BQRuntime;
import io.bootique.jdbc.JdbcModule;
import io.bootique.metrics.health.HealthCheckModule;
import io.bootique.test.junit.BQModuleProviderChecker;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import static com.google.common.collect.ImmutableList.of;

public class InstrumentedJdbcModuleProviderTest {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(InstrumentedJdbcModuleProvider.class);
    }

    @Test
    public void testModuleDeclaresDependencies() {
        final BQRuntime bqRuntime = testFactory.app().module(new InstrumentedJdbcModuleProvider()).createRuntime();
        BQModuleProviderChecker.testModulesLoaded(bqRuntime, of(
                JdbcModule.class,
                HealthCheckModule.class
        ));
    }
}
