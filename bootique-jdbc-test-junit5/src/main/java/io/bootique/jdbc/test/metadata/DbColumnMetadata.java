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
package io.bootique.jdbc.test.metadata;

/**
 * @since 2.0
 */
public class DbColumnMetadata {

    private String name;
    private int type;
    private boolean pk;
    private boolean nullable;

    public DbColumnMetadata(String name, int type, boolean pk, boolean nullable) {
        this.name = name;
        this.type = type;
        this.pk = pk;
        this.nullable = nullable;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public boolean isPk() {
        return pk;
    }

    /**
     * @since 0.7
     */
    public boolean isNullable() {
        return nullable;
    }
}
