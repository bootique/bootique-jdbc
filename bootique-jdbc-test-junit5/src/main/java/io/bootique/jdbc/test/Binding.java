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

package io.bootique.jdbc.test;

import io.bootique.jdbc.test.metadata.DbColumnMetadata;

import java.sql.PreparedStatement;

/**
 * @since 2.0
 */
public class Binding {

    private DbColumnMetadata column;
    private Object value;

    public Binding(DbColumnMetadata column, Object value) {
        this.column = column;
        this.value = value;
    }

    public void bind(PreparedStatement statement, int position) {
        column.bind(statement, position, value);
    }

    public DbColumnMetadata getColumn() {
        return column;
    }

    public Object getValue() {
        return value;
    }
}
