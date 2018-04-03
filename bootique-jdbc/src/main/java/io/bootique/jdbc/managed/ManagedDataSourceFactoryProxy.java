package io.bootique.jdbc.managed;

import com.fasterxml.jackson.annotation.JsonTypeName;
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
import io.bootique.BootiqueException;
import io.bootique.annotation.BQConfig;
import io.bootique.jackson.JacksonService;
import io.bootique.jdbc.jackson.ManagedDataSourceFactoryProxyDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A default implementation of {@link ManagedDataSourceFactory} that is used when no explicit factory is specified for
 * a given configuration. It looks for concrete factories in DI, and if there is one and only one such factory, uses it
 * as a delegate for DataSource creation. If there are no such factories, or if there's more than one, an exception is
 * thrown.
 *
 * @since 0.25
 */
// TODO: this is a generic class and most parts of it can be used in other abstract proxies... pull up to the core bootique module.
@BQConfig("Default JDBC DataSource configuration.")
@JsonDeserialize(using = ManagedDataSourceFactoryProxyDeserializer.class)
public class ManagedDataSourceFactoryProxy implements ManagedDataSourceFactory {

    private JsonNode jsonNode;

    public ManagedDataSourceFactoryProxy(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }

    // Reduces passed set to the leaves in the inheritance hierarchy that have no subclasses
    static Set<Class<? extends ManagedDataSourceFactory>> leafFactories(Set<Class<? extends ManagedDataSourceFactory>> allFactories) {

        Set<Class<? extends ManagedDataSourceFactory>> leafFactories = new HashSet<>(allFactories);
        for (Class<? extends ManagedDataSourceFactory> factory : allFactories) {
            leafFactories.remove(factory.getSuperclass());
        }

        return leafFactories;
    }

    @Override
    public Optional<ManagedDataSourceSupplier> create(String dataSourceName, Injector injector) {
        return createDataSourceFactory(injector).create(dataSourceName, injector);
    }

    private ManagedDataSourceFactory createDataSourceFactory(Injector injector) {

        Class<? extends ManagedDataSourceFactory> factoryType = delegateFactoryType(injector);
        JavaType jacksonType = TypeFactory.defaultInstance().constructType(factoryType);
        ObjectMapper mapper = createObjectMapper(injector);
        JsonNode nodeWithType = jsonNodeWithType(getTypeLabel(factoryType));

        try {
            return mapper.readValue(new TreeTraversingParser(nodeWithType, mapper), jacksonType);
        } catch (IOException e) {
            throw new BootiqueException(1, "Deserialization of JDBC DataSource configuration failed.", e);
        }
    }

    private String getTypeLabel(Class<? extends ManagedDataSourceFactory> factoryType) {

        // TODO: see TODO in ConfigMetadataCompiler ... at least maybe create a public API for this in Bootique to
        // avoid parsing annotations inside the modules...
        JsonTypeName typeName = factoryType.getAnnotation(JsonTypeName.class);

        if (typeName == null) {
            throw new BootiqueException(1, "Invalid ManagedDataSourceFactory:  "
                    + factoryType.getName()
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

    private Class<? extends ManagedDataSourceFactory> delegateFactoryType(Injector injector) {

        Key<Set<Class<? extends ManagedDataSourceFactory>>> setKey = Key
                .get(new TypeLiteral<Set<Class<? extends ManagedDataSourceFactory>>>() {
                });

        Set<Class<? extends ManagedDataSourceFactory>> allFactories = injector.getProvider(setKey).get();

        // the resulting set should contain this class plus one or more concrete ManagedDataSourceFactory implementors.
        // We can guess the default only if there's a single implementor.
        Set<Class<? extends ManagedDataSourceFactory>> set = leafFactories(allFactories);

        switch (set.size()) {
            case 0:
                throw new BootiqueException(1, "No concrete 'bootique-jdbc' implementation found. " +
                        "You will need to add one (such as 'bootique-jdbc-tomcat', etc.) as an application dependency.");
            case 1:
                return set.iterator().next();
            default:

                List<String> labels = new ArrayList<>(set.size());
                set.forEach(f -> labels.add(getTypeLabel(f)));

                throw new BootiqueException(1, "More than one 'bootique-jdbc' implementation is found. There's no single default. " +
                        "As a result each DataSource configuration must provide a 'type' property. Valid 'type' values: " + labels);
        }
    }
}