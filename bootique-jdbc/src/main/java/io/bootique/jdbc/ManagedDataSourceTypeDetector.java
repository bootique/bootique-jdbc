/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.bootique.jdbc;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.bootique.BootiqueException;
import io.bootique.config.jackson.JsonConfigurationLoader;
import io.bootique.config.jackson.PropertiesConfigurationLoader;
import io.bootique.di.Injector;
import io.bootique.di.Key;
import io.bootique.di.TypeLiteral;
import io.bootique.jdbc.managed.ManagedDataSourceFactory;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A configuration loader extension that fills missing "type" properties of DataSources.
 *
 * @since 3.0
 */
class ManagedDataSourceTypeDetector implements JsonConfigurationLoader {

    private static final String TYPE_FIELD = "type";

    // this ordering places us at the end of the standard loader chain
    public static int ORDER = PropertiesConfigurationLoader.ORDER + 1000;

    private final Injector injector;

    @Inject
    public ManagedDataSourceTypeDetector(Injector injector) {
        this.injector = injector;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public JsonNode updateConfiguration(JsonNode mutableInput) {

        JsonNode dataSources = mutableInput.get(JdbcModule.CONFIG_PREFIX);
        if (dataSources == null || !dataSources.isObject() || dataSources.isEmpty()) {
            return mutableInput;
        }

        List<ObjectNode> noTypes = new ArrayList<>(dataSources.size());
        for (JsonNode ds : dataSources) {
            if (ds.isObject() && ds.get(TYPE_FIELD) == null) {
                noTypes.add((ObjectNode) ds);
            }
        }

        if (noTypes.isEmpty()) {
            return mutableInput;
        }

        String type = defaultJsonTypeName();
        for (ObjectNode ds : noTypes) {
            ds.put(TYPE_FIELD, type);
        }

        return mutableInput;
    }

    private String defaultJsonTypeName() {

        Key<Set<Class<? extends ManagedDataSourceFactory>>> setKey = Key.get(new TypeLiteral<>() {
        });

        Set<Class<? extends ManagedDataSourceFactory>> allFactories = injector.getProvider(setKey).get();

        // the resulting set should contain this class plus one or more concrete ManagedDataSourceFactory implementors.
        // We can guess the default only if there's a single implementor.
        Set<Class<? extends ManagedDataSourceFactory>> types = leafFactories(allFactories);

        return switch (types.size()) {
            case 0 -> throw new BootiqueException(1, """
                    No concrete 'bootique-jdbc' implementation found. You will need to add one \
                    (such as 'bootique-jdbc-hikaricp', etc.) as an application dependency.""");
            case 1 -> jsonTypeName(types.iterator().next());
            default -> {
                List<String> typeNames = types.stream().map(ManagedDataSourceTypeDetector::jsonTypeName).toList();
                throw new BootiqueException(1, """
                        More than one 'bootique-jdbc' implementation is found. There's no single default. \
                        As a result, each DataSource configuration must provide an explicit 'type' property. \
                        Supported types: """ + typeNames);
            }
        };
    }

    static String jsonTypeName(Class<?> type) {
        JsonTypeName typeName = type.getAnnotation(JsonTypeName.class);
        if (typeName == null) {
            throw new BootiqueException(1, "Invalid ManagedDataSourceFactory:  "
                    + type.getName()
                    + ". Not annotated with @JsonTypeName.");
        }

        return typeName.value();
    }

    // Reduces passed set to the leaves in the inheritance hierarchy that have no subclasses
    static Set<Class<? extends ManagedDataSourceFactory>> leafFactories(
            Set<Class<? extends ManagedDataSourceFactory>> allFactories) {

        Set<Class<? extends ManagedDataSourceFactory>> leafFactories = new HashSet<>(allFactories);
        for (Class<? extends ManagedDataSourceFactory> factory : allFactories) {
            leafFactories.remove(factory.getSuperclass());
        }

        return leafFactories;
    }
}
