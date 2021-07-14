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

import io.bootique.jdbc.test.Column;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Objects;

/**
 * @since 2.0.B1
 */
public abstract class ArrayReader implements RowReader {

    public static <T> RowReader create(Column... columns) {

        if (columns.length == 0) {
            return ResultSetRowReader.instance;
        }

        ColumnReader[] columnReaders = new ColumnReader[columns.length];
        for (int i = 0; i < columnReaders.length; i++) {
            columnReaders[i] = forJdbcType(columns[i].getType(), i + 1);
        }

        return new PrecompiledRowReader(columnReaders);
    }

    static ColumnReader forJdbcType(int type, int pos) {
        // TODO: add conversions to java.time types?

        switch (type) {
            // only care about types requiring special handling.. Let the driver handle the rest via "getObject"
            case Types.TIME:
                // MySQL 8 requires a Calendar instance to save local time without undesired TZ conversion.
                // Other DBs work fine with or without the calendar
                return rs -> rs.getTime(pos, Calendar.getInstance());
            case Types.TIMESTAMP:
                // MySQL 8 requires a Calendar instance to save local time without undesired TZ conversion.
                // Other DBs work fine with or without the calendar
                return rs -> rs.getTimestamp(pos, Calendar.getInstance());
            default:
                return rs -> rs.getObject(pos);
        }
    }

    protected ArrayReader() {
    }

    @Override
    public Object[] readRow(ResultSet rs) throws SQLException {

        ColumnReader[] columnReaders = getColumnReaders(rs);

        int width = columnReaders.length;
        Object[] result = new Object[width];

        for (int i = 0; i < width; i++) {
            result[i] = columnReaders[i].read(rs);
        }

        return result;
    }

    protected abstract ColumnReader[] getColumnReaders(ResultSet rs) throws SQLException;

    static class ResultSetRowReader extends ArrayReader {

        static final ResultSetRowReader instance = new ResultSetRowReader();

        @Override
        protected ColumnReader[] getColumnReaders(ResultSet rs) throws SQLException {
            ResultSetMetaData md = rs.getMetaData();
            int w = md.getColumnCount();

            ColumnReader[] readers = new ColumnReader[w];

            for (int i = 0; i < w; i++) {
                int jdbcPos = i + 1;
                readers[i] = ArrayReader.forJdbcType(md.getColumnType(jdbcPos), jdbcPos);
            }

            return readers;
        }


    }

    static class PrecompiledRowReader extends ArrayReader {

        private final ColumnReader[] columnReaders;

        public PrecompiledRowReader(ColumnReader[] columnReaders) {
            this.columnReaders = Objects.requireNonNull(columnReaders);
        }

        @Override
        protected ColumnReader[] getColumnReaders(ResultSet rs) {
            return columnReaders;
        }
    }
}
