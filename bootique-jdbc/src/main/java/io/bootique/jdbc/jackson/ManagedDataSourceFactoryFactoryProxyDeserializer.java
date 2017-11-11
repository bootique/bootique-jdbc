package io.bootique.jdbc.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.bootique.jdbc.managed.ManagedDataSourceFactoryFactoryProxy;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link ManagedDataSourceFactoryFactoryProxy}.
 *
 * @since 0.25
 */
public class ManagedDataSourceFactoryFactoryProxyDeserializer extends StdDeserializer<ManagedDataSourceFactoryFactoryProxy> {

    protected ManagedDataSourceFactoryFactoryProxyDeserializer() {
        super(ManagedDataSourceFactoryFactoryProxy.class);
    }

    @Override
    public ManagedDataSourceFactoryFactoryProxy deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();

        return new ManagedDataSourceFactoryFactoryProxy(node);
    }
}
