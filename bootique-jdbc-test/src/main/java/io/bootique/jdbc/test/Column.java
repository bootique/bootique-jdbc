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

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Column {

    static final int NO_TYPE = Integer.MIN_VALUE;

    private String name;
    private int type;

    public Column(String name) {
        this(name, NO_TYPE);
    }

    public Column(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    protected boolean typeUnknown() {
        return type == NO_TYPE;
    }

    public void bind(PreparedStatement statement, int position, Object value) {

        try {
            if (value == null) {
                bindNull(statement, position);
            } else {
                bindNotNull(statement, position, value);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error binding value for column '" + name + "'", e);
        }
    }

    protected void bindNull(PreparedStatement statement, int position) throws SQLException {

        int jdbcPosition = position + 1;
        int type = this.type;

        if (typeUnknown()) {
            type = statement.getParameterMetaData().getParameterType(jdbcPosition);
        }

        statement.setNull(jdbcPosition, type);
    }

    protected void bindNotNull(PreparedStatement statement, int position, Object value) throws SQLException {

        int jdbcPosition = position + 1;

        if (typeUnknown()) {
            statement.setObject(jdbcPosition, value);
        } else {
            statement.setObject(jdbcPosition, value, type);
        }
    }


}
