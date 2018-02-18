package io.bootique.jdbc.instrumented.hikaricp;

import io.bootique.BQRuntime;
import io.bootique.jdbc.JdbcModule;
import io.bootique.jdbc.instrumented.InstrumentedJdbcModule;
import io.bootique.metrics.MetricsModule;
import io.bootique.test.junit.BQModuleProviderChecker;
import io.bootique.test.junit.BQRuntimeChecker;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

public class HikariCPInstrumentedModuleProviderTest {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(HikariCPInstrumentedModuleProvider.class);
    }

    @Test
    public void testModuleDeclaresDependencies() {
        final BQRuntime bqRuntime = testFactory.app().module(new HikariCPInstrumentedModuleProvider()).createRuntime();
        BQRuntimeChecker.testModulesLoaded(bqRuntime,
                InstrumentedJdbcModule.class,
                MetricsModule.class,
                JdbcModule.class
        );
    }
}
