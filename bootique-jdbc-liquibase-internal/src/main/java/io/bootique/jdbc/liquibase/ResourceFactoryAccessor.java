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

package io.bootique.jdbc.liquibase;

import io.bootique.resource.ResourceFactory;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A Liquibase {@link ResourceAccessor} that treats paths as Bootique resource URLs that should be processed via
 * {@link ResourceFactory}.
 *
 * @since 2.0
 */
public class ResourceFactoryAccessor implements ResourceAccessor {

    @Override
    public List<Resource> getAll(String path) {
        return new ResourceFactory(path).getUrls()
                .stream()
                .map(u -> new UrlResource(path, u))
                .collect(Collectors.toList());
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public List<String> describeLocations() {
        // TODO: should we provide a meaningful implementation?
        return List.of();
    }

    @Override
    public List<Resource> search(String path, boolean recursive) {
        // TODO: should we provide a meaningful implementation?
        throw new UnsupportedOperationException("'search' operation is not defined");
    }
}
