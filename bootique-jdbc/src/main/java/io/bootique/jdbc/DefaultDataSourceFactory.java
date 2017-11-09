package io.bootique.jdbc;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.inject.Inject;
import io.bootique.annotation.BQConfig;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.config.TypesFactory;
import io.bootique.jackson.JacksonService;

import java.io.IOException;
import java.util.Collection;

@BQConfig("Default JDBC DataSource configuration.")
@JsonDeserialize(using = DefaultDataSourceFactoryDeserializer.class)
public class DefaultDataSourceFactory implements CPDataSourceFactory {

    private JsonNode jsonNode;
    private CPDataSourceFactory factory;

    @Inject
    private static JacksonService jacksonService;
    @Inject
    private static TypesFactory<PolymorphicConfiguration> typesFactory;

    private TypeFactory typeFactory = TypeFactory.defaultInstance();


    public DefaultDataSourceFactory(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }

    @Override
    public ManagedDataSource createDataSource() {
        if (factory == null) {
            factory = createDataSourceFactory();
        }
        return factory.createDataSource();
    }

    @Override
    public boolean isPartial() {
       return false;

    }

    @Override
    public String getUrl() {
        if (factory == null) {
            factory = createDataSourceFactory();
        }
        return factory.getUrl();
    }

    private CPDataSourceFactory createDataSourceFactory() {
        ObjectMapper mapper = jacksonService.newObjectMapper();

        Collection<Class<? extends PolymorphicConfiguration>> types = typesFactory.getTypes();
        JavaType jacksonType = typeFactory.constructType(types.iterator().next());

        if (types.size() == 1) {
            try {
                factory = mapper.readValue(
                        new TreeTraversingParser(jsonNode, mapper), jacksonType);

                return factory;

            } catch (IOException e) {
                throw new RuntimeException("Unexpected exception");
            }
        }

        throw new IllegalArgumentException("type not defined");
    }
}
