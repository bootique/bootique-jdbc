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
package io.bootique.jdbc.junit5.metadata;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;

/**
 * @since 2.0
 */
public class DbColumnMetadata {

    public static final int NO_TYPE = Integer.MIN_VALUE;

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

    public boolean isNullable() {
        return nullable;
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
        switch (type) {
            case NO_TYPE:
                statement.setObject(jdbcPosition, value);
                break;
            case Types.TIME:
                // MySQL 8 requires a Calendar instance to save local time without undesired TZ conversion.
                // Other DBs work fine with or without the calendar
                if (value instanceof Time t) {
                    statement.setTime(jdbcPosition, t, Calendar.getInstance());
                } else {
                    statement.setObject(jdbcPosition, value, type);
                }
                break;
            case Types.TIMESTAMP:
                // MySQL 8 requires a Calendar instance to save local time without undesired TZ conversion.
                // Other DBs work fine with or without the calendar
                if (value instanceof Timestamp ts) {
                    statement.setTimestamp(jdbcPosition, ts, Calendar.getInstance());
                } else {
                    statement.setObject(jdbcPosition, value, type);
                }
                break;
            case Types.DECIMAL:
                // insert as BigDecimal if possible, otherwise may lose precision on Derby
                if (value instanceof BigDecimal bd) {
                    statement.setBigDecimal(jdbcPosition, bd);
                } else {
                    statement.setObject(jdbcPosition, value, type);
                }
                break;
            default:
                statement.setObject(jdbcPosition, value, type);
        }
    }

    protected boolean typeUnknown() {
        return type == NO_TYPE;
    }
}
