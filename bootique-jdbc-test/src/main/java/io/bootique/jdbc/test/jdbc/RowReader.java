package io.bootique.jdbc.test.jdbc;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * @param <T>
 * @since 0.24
 */
public interface RowReader<T> {

    T readRow(ResultSet rs) throws SQLException;

    static RowReader<Object[]> arrayReader(int width) {
        return rs -> {
            Object[] result = new Object[width];

            for (int i = 1; i <= width; i++) {
                result[i - 1] = rs.getObject(i);
            }

            return result;
        };
    }

    static RowReader<Integer> intReader() {
        return rs -> rs.getInt(1);
    }

    static RowReader<Long> longReader() {
        return rs -> rs.getLong(1);
    }

    static RowReader<String> stringReader() {
        return rs -> rs.getString(1);
    }

    static RowReader<Object> objectReader() {
        return rs -> rs.getObject(1);
    }

    static RowReader<Byte> byteReader() {
        return rs -> rs.getByte(1);
    }

    static RowReader<byte[]> bytesReader() {
        return rs -> rs.getBytes(1);
    }

    static RowReader<Double> doubleReader() {
        return rs -> rs.getDouble(1);
    }

    static RowReader<Boolean> booleanReader() {
        return rs -> rs.getBoolean(1);
    }

    static RowReader<Date> dateReader() {
        return rs -> rs.getDate(1);
    }

    static RowReader<Time> timeReader() {
        return rs -> rs.getTime(1);
    }

    static RowReader<Timestamp> timestampReader() {
        return rs -> rs.getTimestamp(1);
    }
}
