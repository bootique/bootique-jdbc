package io.bootique.jdbc.test;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import io.bootique.BQRuntime;
import io.bootique.jdbc.DataSourceListener;
import io.bootique.jdbc.JdbcModule;
import io.bootique.log.BootLogger;
import io.bootique.test.junit.BQTestFactory;
import org.junit.ClassRule;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class JdbcTestModuleIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();

    @Test
    public void testListenersClasses_Injected() {
        BQRuntime runtime = TEST_FACTORY.app("-c", "classpath:io/bootique/jdbc/test/dummy-ds.yml")
                .autoLoadModules()
                .module(binder -> {
                    JdbcModule.extend(binder)
                            .addDataSourceListener(new TestDataSourceListener1())
                            .addDataSourceListener(new TestDataSourceListener2());
                }).createRuntime();


        TypeLiteral<Set<io.bootique.jdbc.DataSourceListener>> typeLiteral = new TypeLiteral<Set<io.bootique.jdbc.DataSourceListener>>() {
        };

        Set<io.bootique.jdbc.DataSourceListener> set = runtime.getInstance(Key.get(typeLiteral));
        assertEquals(set.size(), 3);
    }

    @Test
    public void testListeners_Injected() {
        BQRuntime runtime = TEST_FACTORY.app("-c", "classpath:io/bootique/jdbc/test/dummy-ds.yml")
                .autoLoadModules()
                .module(new Module() {

                    @Override
                    public void configure(Binder binder) {
                        JdbcModule.extend(binder)
                                .addDataSourceListener(TestDataSourceListener3.class)
                                .addDataSourceListener(TestDataSourceListener4.class);
                    }

                    @Singleton
                    @Provides
                    TestDataSourceListener3 provideListener3(BootLogger bootLogger) {
                        return new TestDataSourceListener3();
                    }

                    @Singleton
                    @Provides
                    TestDataSourceListener4 provideListener4(BootLogger bootLogger) {
                        return new TestDataSourceListener4();
                    }

                }).createRuntime();


        TypeLiteral<Set<io.bootique.jdbc.DataSourceListener>> typeLiteral = new TypeLiteral<Set<io.bootique.jdbc.DataSourceListener>>() {
        };

        Set<io.bootique.jdbc.DataSourceListener> set = runtime.getInstance(Key.get(typeLiteral));
        assertEquals(set.size(), 3);
    }

    static class TestDataSourceListener1 implements DataSourceListener {

        public TestDataSourceListener1() {
        }


        @Override
        public void afterStartup(String name, String jdbcUrl, DataSource dataSource) {
            System.out.print(name + "after started up!\n");
        }

    }

    static class TestDataSourceListener2 implements DataSourceListener {
    }

    static class TestDataSourceListener3 implements DataSourceListener {
    }

    static class TestDataSourceListener4 implements DataSourceListener {
    }
}
