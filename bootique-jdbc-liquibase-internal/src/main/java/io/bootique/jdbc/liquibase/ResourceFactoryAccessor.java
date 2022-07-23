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
import liquibase.resource.InputStreamList;
import liquibase.resource.ResourceAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * A Liquibase {@link ResourceAccessor} that treats paths as Bootique resource URLs that should be processed via
 * {@link ResourceFactory}.
 *
 * @since 2.0
 */
public class ResourceFactoryAccessor implements ResourceAccessor {

    @Override
    public InputStream openStream(String relativeTo, String streamPath) throws IOException {
        return toUrl(relativeTo, streamPath).openStream();
    }

    @Override
    public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
        URL url = toUrl(relativeTo, streamPath);
        try {
            return new InputStreamList(url.toURI(), url.openStream());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Can't convert to a URI: " + url);
        }
    }

    protected URL toUrl(String relativeTo, String streamPath) {

        // TODO: make sure there's a slash between relativeTo and streamPath
        String path = relativeTo != null
                ? relativeTo + streamPath
                : streamPath;

        return new ResourceFactory(path).getUrl();
    }

    @Override
    public SortedSet<String> describeLocations() {
        // TODO: anything more meaningful?
        return new TreeSet<>();
    }

    @Override
    public SortedSet<String> list(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
        throw new UnsupportedOperationException("'list' operation is not defined");
    }
}
