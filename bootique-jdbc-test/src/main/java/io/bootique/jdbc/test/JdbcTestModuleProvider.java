package io.bootique.jdbc.test;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;

import io.bootique.jdbc.tomcat.instrumented.TomcatCPInstrumentedModule;
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
    public Collection<Class<? extends Module>> overrides() {
        return Collections.singleton(TomcatCPInstrumentedModule.class);
    }

    @Override
    public Map<String, Type> configs() {
        return Collections.singletonMap("jdbctest", DatabaseChannelFactoryFactory.class);
    }
}
