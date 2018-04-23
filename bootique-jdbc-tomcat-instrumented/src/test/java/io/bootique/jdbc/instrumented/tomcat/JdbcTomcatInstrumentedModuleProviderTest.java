package io.bootique.jdbc.instrumented.tomcat;

import io.bootique.BQRuntime;
import io.bootique.jdbc.JdbcModule;
import io.bootique.jdbc.tomcat.JdbcTomcatModule;
import io.bootique.metrics.MetricsModule;
import io.bootique.test.junit.BQModuleProviderChecker;
import io.bootique.test.junit.BQRuntimeChecker;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

public class JdbcTomcatInstrumentedModuleProviderTest {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testAutoLoadable() {
        BQModuleProviderChecker.testAutoLoadable(JdbcTomcatInstrumentedModuleProvider.class);
    }

    @Test
    public void testModuleDeclaresDependencies() {
        final BQRuntime bqRuntime = testFactory.app().module(new JdbcTomcatInstrumentedModuleProvider()).createRuntime();
        BQRuntimeChecker.testModulesLoaded(bqRuntime,
                JdbcModule.class,
                JdbcTomcatModule.class,
                MetricsModule.class
        );
    }
}
