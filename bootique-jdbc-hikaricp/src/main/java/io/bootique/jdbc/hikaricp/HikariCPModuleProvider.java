package io.bootique.jdbc.hikaricp;


import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.jdbc.JdbcModule;
import io.bootique.type.TypeRef;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class HikariCPModuleProvider implements BQModuleProvider {
    @Override
    public Module module() {
        return new HikariCPModule("jdbc");
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder();

        //TODO: add module description
    }

    @Override
    public Map<String, Type> configs() {
        TypeRef<Map<String, HikariCPDataSourceFactory>> type = new TypeRef<Map<String, HikariCPDataSourceFactory>>() {
        };
        return Collections.singletonMap("jdbc", type.getType());
    }

    @Override
    public Collection<Class<? extends Module>> overrides() {
        return Collections.singleton(JdbcModule.class);
    }

}
