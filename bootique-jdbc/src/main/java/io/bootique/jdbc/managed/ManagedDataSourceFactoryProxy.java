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

package io.bootique.jdbc.managed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import io.bootique.BootiqueException;
import io.bootique.annotation.BQConfig;
import io.bootique.di.Injector;
import io.bootique.di.Key;
import io.bootique.di.TypeLiteral;
import io.bootique.jackson.JacksonService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A default implementation of {@link ManagedDataSourceFactory} that is used when no explicit factory is specified for
 * a given configuration. It looks for concrete factories in DI, and if there is one and only one such factory, uses it
 * as a delegate for DataSource creation. If there are no such factories, or if there's more than one, an exception is
 * thrown.
 */
@BQConfig("Default JDBC DataSource configuration.")
public class ManagedDataSourceFactoryProxy implements ManagedDataSourceFactory {

    private final JsonNode jsonNode;

    @JsonCreator
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
    public ManagedDataSourceStarter create(String dataSourceName, Injector injector) {
        return createDataSourceFactory(injector).create(dataSourceName, injector);
    }

    private ManagedDataSourceFactory createDataSourceFactory(Injector injector) {

        Class<? extends ManagedDataSourceFactory> factoryType = delegateFactoryType(injector);
        ObjectMapper mapper = createObjectMapper(injector);

        // Disables all annotations to prevent the following exception:
        // "Class io.bootique.jdbc.managed.ManagedDataSourceFactoryProxy not subtype of [simple type, class com.foo.MyFactory]"
        // This should work, as we already know the subclass to instantiate. But this will ignore any custom deserializers
        // on factories, which seems like a minor limitation.

        mapper.disable(MapperFeature.USE_ANNOTATIONS);

        ManagedDataSourceFactory factory;

        try {
            factory = factoryType.getDeclaredConstructor().newInstance();
            mapper.readerForUpdating(factory).readValue(new TreeTraversingParser(jsonNode, mapper), factoryType);
        } catch (Exception e) {
            throw new BootiqueException(1, "Deserialization of JDBC DataSource configuration failed.", e);
        }

        return factory;
    }

    private ObjectMapper createObjectMapper(Injector injector) {
        return injector.getInstance(JacksonService.class).newObjectMapper();
    }

    private Class<? extends ManagedDataSourceFactory> delegateFactoryType(Injector injector) {

        Key<Set<Class<? extends ManagedDataSourceFactory>>> setKey = Key.get(new TypeLiteral<>() {
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
}