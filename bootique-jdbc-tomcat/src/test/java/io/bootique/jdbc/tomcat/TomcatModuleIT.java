package io.bootique.jdbc.tomcat;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TomcatModuleIT {
    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testPartialConfigsExcluded() {

        BQRuntime runtime = testFactory.app("-c", "classpath:dummy-3ds.yml").autoLoadModules().createRuntime();
        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);

        Set<String> names = new HashSet<>(factory.allNames());
        assertEquals(new HashSet<>(Arrays.asList("fullds1", "fullds2")), names);
    }

    @Test
    @Ignore
    public void testPartialConfigsExcluded_Vars() {

        BQRuntime runtime = testFactory.app("-c", "classpath:dummy-2ds.yml")
                .autoLoadModules()
                .module(b -> {
                    BQCoreModule.extend(b)
                            .setVar("BQ_JDBC_PARTIAL_PASSWORD", "p1")
                            .setVar("BQ_JDBC_FULLDS2_PASSWORD", "p2")
                            .setVar("BQ_JDBC_FULLDSVARS_URL", "jdbc:dummy");
                })
                .createRuntime();
        DataSourceFactory factory = runtime.getInstance(DataSourceFactory.class);

        Set<String> names = new HashSet<>(factory.allNames());
        assertEquals(new HashSet<>(Arrays.asList("fullds1", "fullds2", "FULLDSVARS")), names);
    }
}
