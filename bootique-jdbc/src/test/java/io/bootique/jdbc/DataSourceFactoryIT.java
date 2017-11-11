package io.bootique.jdbc;

import com.google.inject.ProvisionException;
import io.bootique.BQRuntime;
import io.bootique.BootiqueException;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
}
