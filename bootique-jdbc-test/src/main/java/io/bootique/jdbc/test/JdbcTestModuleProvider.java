package io.bootique.jdbc.test;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;
import io.bootique.jdbc.JdbcModuleProvider;
import io.bootique.jdbc.test.runtime.DatabaseChannelFactoryFactory;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @since 0.12
 */
public class JdbcTestModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new JdbcTestModule();
    }

    @Override
    public Map<String, Type> configs() {
        return Collections.singletonMap("jdbctest", DatabaseChannelFactoryFactory.class);
    }

    @Override
    public Collection<BQModuleProvider> dependencies() {
        return Collections.singletonList(
                new JdbcModuleProvider()
        );
    }
}
