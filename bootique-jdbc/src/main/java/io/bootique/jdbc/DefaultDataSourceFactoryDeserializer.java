package io.bootique.jdbc;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class DefaultDataSourceFactoryDeserializer extends StdDeserializer<DefaultManagedDataSourceFactory> {

    protected DefaultDataSourceFactoryDeserializer() {
        super(DefaultManagedDataSourceFactory.class);
    }

    @Override
    public DefaultManagedDataSourceFactory deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();

        return new DefaultManagedDataSourceFactory(node);
    }
}
