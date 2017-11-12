package io.bootique.jdbc;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import io.bootique.BQRuntime;
import io.bootique.BootiqueException;
import io.bootique.jdbc.managed.ManagedDataSourceFactory;
import io.bootique.jdbc.managed.ManagedDataSourceSupplier;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class DataSourceFactoryIT {

    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testForName_NoImpl() {

        BQRuntime runtime = testFactory.app("-c", "classpath:DataSourceFactoryIT_notype.yml")
                .autoLoadModules()
                .createRuntime();

        try {
            runtime.getInstance(DataSourceFactory.class).forName("ds1");
            fail("Exception expected");
        } catch (ProvisionException e) {
            assertTrue(e.getCause() instanceof BootiqueException);
        }
    }

    @Test
    public void testForName_SingleImpl() {

        BQRuntime runtime = testFactory.app("-c", "classpath:DataSourceFactoryIT_notype.yml")
                .autoLoadModules()
                .module(b -> JdbcModule.extend(b).addFactoryType(Factory1.class))
                .createRuntime();


        DataSource ds = runtime.getInstance(DataSourceFactory.class).forName("ds1");
        assertNotNull(ds);
    }

    @Test
    public void testForName_MultiImpl() {

        BQRuntime runtime = testFactory.app("-c", "classpath:DataSourceFactoryIT_notype.yml")
                .autoLoadModules()
                .module(b -> JdbcModule.extend(b).addFactoryType(Factory1.class).addFactoryType(Factory2.class))
                .createRuntime();

        try {
            runtime.getInstance(DataSourceFactory.class).forName("ds1");
            fail("Exception expected");
        } catch (ProvisionException e) {
            assertTrue(e.getCause() instanceof BootiqueException);
        }
    }

    @JsonTypeName("f1")
    public static class Factory1 implements ManagedDataSourceFactory {

        private String url;

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public Optional<ManagedDataSourceSupplier> create(Injector injector) {
            return Optional.of(new ManagedDataSourceSupplier(
                    url,
                    () -> mock(DataSource.class),
                    ds -> {
                    }));
        }
    }

    @JsonTypeName("f2")
    public static class Factory2 implements ManagedDataSourceFactory {

        private String url;

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public Optional<ManagedDataSourceSupplier> create(Injector injector) {
            return Optional.of(new ManagedDataSourceSupplier(
                    url,
                    () -> mock(DataSource.class),
                    ds -> {
                    }));
        }
    }
}
