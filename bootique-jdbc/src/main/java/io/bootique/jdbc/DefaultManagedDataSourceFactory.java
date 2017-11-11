package io.bootique.jdbc;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.bootique.annotation.BQConfig;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.config.TypesFactory;
import io.bootique.jackson.JacksonService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@BQConfig("Default JDBC DataSource configuration.")
@JsonDeserialize(using = DefaultDataSourceFactoryDeserializer.class)
public class DefaultManagedDataSourceFactory implements ManagedDataSourceFactory {

    private JsonNode jsonNode;
    private ManagedDataSourceFactory factory;

    public DefaultManagedDataSourceFactory(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }

    @Override
    public ManagedDataSource createDataSource(String name, Injector injector, Collection<DataSourceListener> dataSourceListeners) {
        if (factory == null) {
            factory = createDataSourceFactory(injector);
        }

        return factory.createDataSource(name, injector, dataSourceListeners);
    }

    private ManagedDataSourceFactory createDataSourceFactory(Injector injector) {
        ObjectMapper mapper = injector.getInstance(JacksonService.class).newObjectMapper();

        TypeLiteral<TypesFactory<PolymorphicConfiguration>> typesFactoryTypeLiteral = new TypeLiteral<TypesFactory<PolymorphicConfiguration>>() {
        };
        Collection<Class<? extends PolymorphicConfiguration>> types = injector.getProvider(Key.get(typesFactoryTypeLiteral)).get().getTypes();
        TypeFactory typeFactory = TypeFactory.defaultInstance();

        JavaType jacksonType = typeFactory.constructType(types.iterator().next());

        if (filterTypes(types) == 1) {
            try {
                JsonNode copy = jsonNode.deepCopy();
                ((ObjectNode) copy).put("class", jacksonType.getRawClass().getName());

                factory = mapper.readValue(
                        new TreeTraversingParser(copy, mapper), jacksonType);

                return factory;
            } catch (IOException e) {
                throw new RuntimeException("Deserialization of JDBC data source configuration failed.", e);
            }
        }

        throw new IllegalArgumentException("Ambiguous connection pools! JDBC data source configuration doesn't explicitly define \"type\" of CP.");
    }

    /**
     * Filters polymorphic types excluding not {@link ManagedDataSourceFactory}.
     * Address issue with bq-jdbc-metrics providing polymorphic config for reporters.
     *
     * @param types to be filtered
     * @return number of available connection pools
     */
    private int filterTypes(Collection<Class<? extends PolymorphicConfiguration>> types) {
        List<Class<? extends PolymorphicConfiguration>> dataSourceFactories = new ArrayList<>();

        for (Class<? extends PolymorphicConfiguration> configuration : types) {
            if (ManagedDataSourceFactory.class.isAssignableFrom(configuration)) {
                dataSourceFactories.add(configuration);
            }
        }

        return dataSourceFactories.size();
    }
}
