package io.bootique.jdbc;

import io.bootique.BQCoreModule;
import io.bootique.test.BQTestRuntime;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class JdbcModuleIT {

    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testPartialConfigsExcluded() {

        BQTestRuntime runtime = testFactory.app("-c", "classpath:dummy-3ds.yml").autoLoadModules().createRuntime();
        DataSourceFactory factory = runtime.getRuntime().getInstance(DataSourceFactory.class);

        Set<String> names = new HashSet<>(factory.allNames());
        assertEquals(new HashSet<>(Arrays.asList("fullds1", "fullds2")), names);
    }

    @Test
    public void testPartialConfigsExcluded_Vars() {

        BQTestRuntime runtime = testFactory.app("-c", "classpath:dummy-2ds.yml")
                .autoLoadModules()
                .module(b -> {
                    BQCoreModule.contributeVariables(b).addBinding("BQ_JDBC_PARTIAL_PASSWORD").toInstance("p1");
                    BQCoreModule.contributeVariables(b).addBinding("BQ_JDBC_FULLDS2_PASSWORD").toInstance("p2");
                    BQCoreModule.contributeVariables(b).addBinding("BQ_JDBC_FULLDSVARS_URL").toInstance("jdbc:dummy");
                })
                .createRuntime();
        DataSourceFactory factory = runtime.getRuntime().getInstance(DataSourceFactory.class);

        Set<String> names = new HashSet<>(factory.allNames());
        assertEquals(new HashSet<>(Arrays.asList("fullds1", "fullds2", "FULLDSVARS")), names);
    }
}
