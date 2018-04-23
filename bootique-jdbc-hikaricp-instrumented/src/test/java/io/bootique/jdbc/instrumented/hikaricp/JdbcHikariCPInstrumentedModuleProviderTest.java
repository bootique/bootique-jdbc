package io.bootique.jdbc.instrumented.hikaricp;

import io.bootique.BQRuntime;
import io.bootique.jdbc.JdbcModule;
import io.bootique.metrics.MetricsModule;
import io.bootique.metrics.health.HealthCheckModule;
import io.bootique.test.junit.BQModuleProviderChecker;
import io.bootique.test.junit.BQRuntimeChecker;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

public class JdbcHikariCPInstrumentedModuleProviderTest {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testAutoLoadable() {
        BQModuleProviderChecker.testAutoLoadable(JdbcHikariCPInstrumentedModuleProvider.class);
    }

    @Test
    public void testModuleDeclaresDependencies() {
        final BQRuntime bqRuntime = testFactory.app().module(new JdbcHikariCPInstrumentedModuleProvider()).createRuntime();
        BQRuntimeChecker.testModulesLoaded(bqRuntime,
                HealthCheckModule.class,
                MetricsModule.class,
                JdbcModule.class
        );
    }
}
