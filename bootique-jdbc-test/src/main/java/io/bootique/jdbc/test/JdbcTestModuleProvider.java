package io.bootique.jdbc.test;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;
import io.bootique.jdbc.instrumented.InstrumentedJdbcModule;

import java.util.Collection;
import java.util.Collections;

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
        return Collections.singleton(InstrumentedJdbcModule.class);
    }
}
