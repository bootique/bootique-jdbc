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

package io.bootique.jdbc.test.jdbc;

import io.bootique.jdbc.test.RowConverter;

import java.sql.*;

/**
 * @param <T>
 * @since 0.24
 */
public interface RowReader<T> {

    T readRow(ResultSet rs) throws SQLException;

    /**
     * @since 2.0.B1
     */
    static <T> RowReader<T> arrayReader(RowConverter<T> converter) {

        return rs -> {
            int width = rs.getMetaData().getColumnCount();
            Object[] result = new Object[width];

            for (int i = 1; i <= width; i++) {
                result[i - 1] = rs.getObject(i);
            }

            return converter.convert(result);
        };
    }


    static RowReader<Object[]> arrayReader(int width) {
        return rs -> {
            Object[] result = new Object[width];

            for (int i = 1; i <= width; i++) {
                result[i - 1] = rs.getObject(i);
            }

            return result;
        };
    }

    /**
     * @deprecated since 2.0.B1. This API was for reading values from a single-column ResultSet used in data assertions
     * in {@link io.bootique.jdbc.test.Table}. This use case was superceded by the
     * {@link io.bootique.jdbc.test.Table#matcher()}, and this method is no longer relevant or useful.
     */
    @Deprecated
    static RowReader<Integer> intReader() {
        return rs -> rs.getInt(1);
    }

    /**
     * @deprecated since 2.0.B1. This API was for reading values from a single-column ResultSet used in data assertions
     * in {@link io.bootique.jdbc.test.Table}. This use case was superceded by the
     * {@link io.bootique.jdbc.test.Table#matcher()}, and this method is no longer relevant or useful.
     */
    @Deprecated
    static RowReader<Long> longReader() {
        return rs -> rs.getLong(1);
    }

    /**
     * @deprecated since 2.0.B1. This API was for reading values from a single-column ResultSet used in data assertions
     * in {@link io.bootique.jdbc.test.Table}. This use case was superceded by the
     * {@link io.bootique.jdbc.test.Table#matcher()}, and this method is no longer relevant or useful.
     */
    @Deprecated
    static RowReader<String> stringReader() {
        return rs -> rs.getString(1);
    }

    /**
     * @deprecated since 2.0.B1. This API was for reading values from a single-column ResultSet used in data assertions
     * in {@link io.bootique.jdbc.test.Table}. This use case was superceded by the
     * {@link io.bootique.jdbc.test.Table#matcher()}, and this method is no longer relevant or useful.
     */
    @Deprecated
    static RowReader<Object> objectReader() {
        return rs -> rs.getObject(1);
    }

    /**
     * @deprecated since 2.0.B1. This API was for reading values from a single-column ResultSet used in data assertions
     * in {@link io.bootique.jdbc.test.Table}. This use case was superceded by the
     * {@link io.bootique.jdbc.test.Table#matcher()}, and this method is no longer relevant or useful.
     */
    @Deprecated
    static RowReader<Byte> byteReader() {
        return rs -> rs.getByte(1);
    }

    /**
     * @deprecated since 2.0.B1. This API was for reading values from a single-column ResultSet used in data assertions
     * in {@link io.bootique.jdbc.test.Table}. This use case was superceded by the
     * {@link io.bootique.jdbc.test.Table#matcher()}, and this method is no longer relevant or useful.
     */
    @Deprecated
    static RowReader<byte[]> bytesReader() {
        return rs -> rs.getBytes(1);
    }

    /**
     * @deprecated since 2.0.B1. This API was for reading values from a single-column ResultSet used in data assertions
     * in {@link io.bootique.jdbc.test.Table}. This use case was superceded by the
     * {@link io.bootique.jdbc.test.Table#matcher()}, and this method is no longer relevant or useful.
     */
    @Deprecated
    static RowReader<Double> doubleReader() {
        return rs -> rs.getDouble(1);
    }

    /**
     * @deprecated since 2.0.B1. This API was for reading values from a single-column ResultSet used in data assertions
     * in {@link io.bootique.jdbc.test.Table}. This use case was superceded by the
     * {@link io.bootique.jdbc.test.Table#matcher()}, and this method is no longer relevant or useful.
     */
    @Deprecated
    static RowReader<Boolean> booleanReader() {
        return rs -> rs.getBoolean(1);
    }

    /**
     * @deprecated since 2.0.B1. This API was for reading values from a single-column ResultSet used in data assertions
     * in {@link io.bootique.jdbc.test.Table}. This use case was superceded by the
     * {@link io.bootique.jdbc.test.Table#matcher()}, and this method is no longer relevant or useful.
     */
    @Deprecated
    static RowReader<Date> dateReader() {
        return rs -> rs.getDate(1);
    }

    /**
     * @deprecated since 2.0.B1. This API was for reading values from a single-column ResultSet used in data assertions
     * in {@link io.bootique.jdbc.test.Table}. This use case was superceded by the
     * {@link io.bootique.jdbc.test.Table#matcher()}, and this method is no longer relevant or useful.
     */
    @Deprecated
    // TODO: affected by #108 when MySQL 8 driver is used
    static RowReader<Time> timeReader() {
        return rs -> rs.getTime(1);
    }

    /**
     * @deprecated since 2.0.B1. This API was for reading values from a single-column ResultSet used in data assertions
     * in {@link io.bootique.jdbc.test.Table}. This use case was superceded by the
     * {@link io.bootique.jdbc.test.Table#matcher()}, and this method is no longer relevant or useful.
     */
    @Deprecated
    // TODO: affected by #108 when MySQL 8 driver is used
    static RowReader<Timestamp> timestampReader() {
        return rs -> rs.getTimestamp(1);
    }
}
