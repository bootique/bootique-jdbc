package io.bootique.jdbc;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class DefaultDataSourceFactoryDeserializer extends StdDeserializer<DefaultDataSourceFactory> {

    protected DefaultDataSourceFactoryDeserializer() {
        super(DefaultDataSourceFactory.class);
    }

    @Override
    public DefaultDataSourceFactory deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();

        return new DefaultDataSourceFactory(node);
    }
}
