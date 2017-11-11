package io.bootique.jdbc.tomcat;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.type.TypeRef;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

public class TomcatJdbcModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new TomcatJdbcModule("jdbc");
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Provides configuration for and access to named TomcatCP DataSources.");
    }

    @Override
    public Map<String, Type> configs() {
        // TODO: config prefix is hardcoded. Refactor away from ConfigModule, and make provider
        // generate config prefix, reusing it in metadata...

        TypeRef<Map<String, TomcatManagedDataSourceFactory>> type = new TypeRef<Map<String, TomcatManagedDataSourceFactory>>() {
        };
        return Collections.singletonMap("jdbc", type.getType());
    }
}
