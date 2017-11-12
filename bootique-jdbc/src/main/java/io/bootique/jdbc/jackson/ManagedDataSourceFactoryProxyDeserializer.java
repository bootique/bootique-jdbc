package io.bootique.jdbc.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.bootique.jdbc.managed.ManagedDataSourceFactoryProxy;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link ManagedDataSourceFactoryProxy}.
 *
 * @since 0.25
 */
public class ManagedDataSourceFactoryProxyDeserializer extends StdDeserializer<ManagedDataSourceFactoryProxy> {

    protected ManagedDataSourceFactoryProxyDeserializer() {
        super(ManagedDataSourceFactoryProxy.class);
    }

    @Override
    public ManagedDataSourceFactoryProxy deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        // TODO: how do we pass a "type" value down to ManagedDataSourceFactoryProxy? It is stripped from JsonNode by
        // Jackson, so we can't distinguish missing "type" from invalid.

        JsonNode node = p.readValueAsTree();

        return new ManagedDataSourceFactoryProxy(node);
    }
}
