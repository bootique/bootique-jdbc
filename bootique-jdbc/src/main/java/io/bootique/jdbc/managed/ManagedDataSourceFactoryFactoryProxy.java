package io.bootique.jdbc.managed;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.bootique.BootiqueException;
import io.bootique.annotation.BQConfig;
import io.bootique.env.Environment;
import io.bootique.jackson.JacksonService;
import io.bootique.jdbc.jackson.ManagedDataSourceFactoryFactoryProxyDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Default implementation of {@link ManagedDataSourceFactoryFactory} that tries to dynamically detect a single concrete
 * factory available at runtime, and them load configuration via it.
 *
 * @since 0.25
 */
@BQConfig("Default JDBC DataSource configuration.")
@JsonDeserialize(using = ManagedDataSourceFactoryFactoryProxyDeserializer.class)
public class ManagedDataSourceFactoryFactoryProxy implements ManagedDataSourceFactoryFactory {

    private JsonNode jsonNode;

    public ManagedDataSourceFactoryFactoryProxy(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }

    @Override
    public Optional<ManagedDataSourceFactory> create(Injector injector) {
        return createDataSourceFactory(injector).create(injector);
    }

    private ManagedDataSourceFactoryFactory createDataSourceFactory(Injector injector) {

        ManagedDataSourceFactoryFactory delegateFactory = delegateFactory(injector);
        JavaType jacksonType = TypeFactory.defaultInstance().constructType(delegateFactory.getClass());
        ObjectMapper mapper = createObjectMapper(injector);
        JsonNode nodeWithType = jsonNodeWithType(getTypeLabel(delegateFactory));

        // TODO: deprecated, should be removed once we stop supporting BQ_ vars...
        // in other words this can be removed when a similar code is removed from JsonNodeConfigurationFactoryProvider
        Environment environment = injector.getInstance(Environment.class);
        if (!environment.frameworkVariables().isEmpty()) {
            // switching to slower CI strategy for mapping properties...
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        }

        try {
            return mapper.readValue(new TreeTraversingParser(nodeWithType, mapper), jacksonType);
        } catch (IOException e) {
            throw new BootiqueException(1, "Deserialization of JDBC DataSource configuration failed.", e);
        }
    }

    private String getTypeLabel(ManagedDataSourceFactoryFactory delegateFactory) {

        // TODO: see TODO in ConfigMetadataCompiler ... at least maybe create a public API for this in Bootique to
        // avoid parsing annotations inside the modules...
        JsonTypeName typeName = delegateFactory.getClass().getAnnotation(JsonTypeName.class);

        if (typeName == null) {
            throw new BootiqueException(1, "Invalid ManagedDataSourceFactoryFactory:  "
                    + delegateFactory.getClass().getName()
                    + ". Not annotated with @JsonTypeName.");
        }

        return typeName.value();
    }

    private JsonNode jsonNodeWithType(String type) {
        JsonNode copy = jsonNode.deepCopy();
        ((ObjectNode) copy).put("type", type);
        return copy;
    }

    private ObjectMapper createObjectMapper(Injector injector) {
        return injector.getInstance(JacksonService.class).newObjectMapper();
    }

    private ManagedDataSourceFactoryFactory delegateFactory(Injector injector) {

        Set<ManagedDataSourceFactoryFactory> factories = injector
                .getProvider(Key.get(new TypeLiteral<Set<ManagedDataSourceFactoryFactory>>() {
                }))
                .get();

        // will contain this class plus one or more concrete ManagedDataSourceFactory implementors. We can guess the
        // default only if there's a single implementor.

        switch (factories.size()) {
            case 0:
                throw new BootiqueException(1, "No concrete 'bootique-jdbc' implementation found. " +
                        "You will need to add one (such as 'bootique-jdbc-tomcat', etc.) as an application dependency.");
            case 1:
                return factories.iterator().next();
            default:

                List<String> labels = new ArrayList<>(factories.size());
                factories.forEach(f -> labels.add(getTypeLabel(f)));

                throw new BootiqueException(1, "Multiple bootique-jdbc implementations found. Each JDBC DataSource " +
                        "configuration must explicitly define \"type\" property. Available types: " + labels);
        }
    }
}
