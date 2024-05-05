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

import liquibase.resource.OpenOptions;
import liquibase.resource.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

class UrlResource implements Resource {

    private final String path;
    private final URL url;

    private volatile URI uri;

    UrlResource(String path, URL url) {
        this.path = Objects.requireNonNull(path);
        this.url = Objects.requireNonNull(url);
    }

    @Override
    public boolean exists() {
        // we don't really know if the URL exists
        return true;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return url.openStream();
    }

    @Override
    public OutputStream openOutputStream(OpenOptions openOptions) {
        throw new UnsupportedOperationException("The resource is not writable: " + url);
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public Resource resolve(String other) {
        throw new UnsupportedOperationException("The resource is not a directory: " + url);
    }

    @Override
    public Resource resolveSibling(String other) {

        String siblingPath = resolveSiblingPath(this.path, other);
        URL siblingUrl;
        try {
            siblingUrl = new URL(resolveSiblingPath(url.toString(), other));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        return new UrlResource(siblingPath, siblingUrl);
    }

    @Override
    public URI getUri() {
        if (uri == null) {
            try {
                uri = url.toURI();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        return uri;
    }

    private String resolveSiblingPath(String path, String other) {
        if (path.contains("/")) {
            return path.replaceFirst("/[^/]*$", "") + "/" + other;
        } else {
            return "/" + other;
        }
    }
}
