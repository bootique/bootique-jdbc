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
import liquibase.resource.ResourceAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Set;


/**
 * A Liquibase {@link ResourceAccessor} that treats paths as Bootique resource URLs that should be processed via
 * {@link ResourceFactory}.
 *
 * @since 2.0
 */
public class ResourceFactoryAccessor implements ResourceAccessor {

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
        URL url = new ResourceFactory(path).getUrl();
        return Collections.singleton(url.openStream());
    }

    @Override
    public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        throw new UnsupportedOperationException("'list' operation is not defined");
    }

    @Override
    public ClassLoader toClassLoader() {
        return getClass().getClassLoader();
    }
}
