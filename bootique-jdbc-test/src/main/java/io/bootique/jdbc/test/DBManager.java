package io.bootique.jdbc.test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to run common DB operations during unit tests.
 *
 * @since 0.12
 */
// Borrowed from Cayenne
public class DBManager {

    protected DataSource dataSource;

    public DBManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Quotes a SQL identifier as appropriate for the given DB. This
     * implementation returns the identifier unchanged, while subclasses can
     * implement a custom quoting strategy.
     */
    protected String quote(String sqlIdentifier) {
        return sqlIdentifier;
    }

    /**
     * Selects a single row.
     */
    public Object[] select(String table, final String[] columns) {

        if (columns.length == 0) {
            throw new IllegalArgumentException("No columns");
        }

        StringBuilder sql = new StringBuilder("select ");
        sql.append(quote(columns[0]));
        for (int i = 1; i < columns.length; i++) {
            sql.append(", ").append(quote(columns[i]));
        }
        sql.append(" from ").append(quote(table));

        return new RowTemplate<Object[]>(this) {

            @Override
            Object[] readRow(ResultSet rs, String sql) throws SQLException {

                Object[] result = new Object[columns.length];
                for (int i = 1; i <= result.length; i++) {
                    result[i - 1] = rs.getObject(i);
                }

                return result;
            }
        }.execute(sql.toString());
    }

    public List<Object[]> selectAll(String table, final String[] columns) {
        if (columns.length == 0) {
            throw new IllegalArgumentException("No columns");
        }

        StringBuilder sql = new StringBuilder("select ");
        sql.append(quote(columns[0]));
        for (int i = 1; i < columns.length; i++) {
            sql.append(", ").append(quote(columns[i]));
        }
        sql.append(" from ").append(quote(table));

        return new ResultSetTemplate<List<Object[]>>(this) {
            @Override
            protected List<Object[]> readResultSet(ResultSet rs, String sql) throws SQLException {

                List<Object[]> result = new ArrayList<Object[]>();
                while (rs.next()) {

                    Object[] row = new Object[columns.length];
                    for (int i = 1; i <= row.length; i++) {
                        row[i - 1] = rs.getObject(i);
                    }

                    result.add(row);
                }

                return result;
            }
        }.execute(sql.toString());
    }

    /**
     * Inserts a single row. Columns types can be null and will be determined
     * from ParameterMetaData in this case. The later scenario will not work if
     * values contains nulls and the DB is Oracle.
     */
    public void insert(String table, String[] columns, Object[] values, int[] columnTypes) {

        if (columns.length != values.length) {
            throw new IllegalArgumentException("Columns and values arrays have different sizes: " + columns.length
                    + " and " + values.length);
        }

        if (columns.length == 0) {
            throw new IllegalArgumentException("No columns");
        }

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(quote(table)).append(" (").append(quote(columns[0]));
        for (int i = 1; i < columns.length; i++) {
            sql.append(", ").append(quote(columns[i]));
        }

        sql.append(") VALUES (?");
        for (int i = 1; i < values.length; i++) {
            sql.append(", ?");
        }
        sql.append(")");

        try (Connection c = getConnection();) {

            String sqlString = sql.toString();
            Logger.log(sqlString);

            ParameterMetaData parameters = null;
            try (PreparedStatement st = c.prepareStatement(sqlString);) {
                for (int i = 0; i < values.length; i++) {

                    if (values[i] == null) {

                        int type;

                        if (columnTypes == null) {

                            // check for the right NULL type
                            if (parameters == null) {
                                parameters = st.getParameterMetaData();
                            }

                            type = parameters.getParameterType(i + 1);
                        } else {
                            type = columnTypes[i];
                        }

                        st.setNull(i + 1, type);
                    } else {
                        st.setObject(i + 1, values[i]);
                    }
                }

                st.executeUpdate();
            }
            c.commit();
        }

    }

    public int deleteAll(String tableName) {
        return delete(tableName).execute();
    }

    public UpdateBuilder update(String tableName) {
        return new UpdateBuilder(this, tableName);
    }

    public DeleteBuilder delete(String tableName) {
        return new DeleteBuilder(this, tableName);
    }

    public int getRowCount(String table) {
        String sql = "select count(*) from " + quote(table);

        return new RowTemplate<Integer>(this) {

            @Override
            Integer readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getInt(1);
            }

        }.execute(sql);
    }

    public String getString(String table, String column) {
        final String sql = "select " + quote(column) + " from " + quote(table);

        return new RowTemplate<String>(this) {

            @Override
            String readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getString(1);
            }

        }.execute(sql);
    }

    public Object getObject(String table, String column) {
        final String sql = "select " + quote(column) + " from " + quote(table);

        return new RowTemplate<Object>(this) {

            @Override
            Object readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getObject(1);
            }

        }.execute(sql);
    }

    public byte getByte(String table, String column) {
        final String sql = "select " + quote(column) + " from " + quote(table);

        return new RowTemplate<Byte>(this) {

            @Override
            Byte readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getByte(1);
            }

        }.execute(sql);
    }

    public byte[] getBytes(String table, String column) {
        final String sql = "select " + quote(column) + " from " + quote(table);

        return new RowTemplate<byte[]>(this) {

            @Override
            byte[] readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getBytes(1);
            }

        }.execute(sql);
    }

    public int getInt(String table, String column) {
        final String sql = "select " + quote(column) + " from " + quote(table);

        return new RowTemplate<Integer>(this) {

            @Override
            Integer readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getInt(1);
            }

        }.execute(sql);
    }

    public long getLong(String table, String column) {
        final String sql = "select " + quote(column) + " from " + quote(table);

        return new RowTemplate<Long>(this) {

            @Override
            Long readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getLong(1);
            }

        }.execute(sql);
    }

    public double getDouble(String table, String column) {
        final String sql = "select " + quote(column) + " from " + quote(table);

        return new RowTemplate<Double>(this) {

            @Override
            Double readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getDouble(1);
            }

        }.execute(sql);
    }

    public boolean getBoolean(String table, String column) {
        final String sql = "select " + quote(column) + " from " + quote(table);

        return new RowTemplate<Boolean>(this) {

            @Override
            Boolean readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getBoolean(1);
            }

        }.execute(sql);
    }

    public java.util.Date getUtilDate(String table, String column) {
        Timestamp ts = getTimestamp(table, column);
        return ts != null ? new java.util.Date(ts.getTime()) : null;
    }

    public java.sql.Date getSqlDate(String table, String column) {
        final String sql = "select " + quote(column) + " from " + quote(table);

        return new RowTemplate<java.sql.Date>(this) {

            @Override
            java.sql.Date readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getDate(1);
            }

        }.execute(sql);
    }

    public Time getTime(String table, String column) {
        final String sql = "select " + quote(column) + " from " + quote(table);

        return new RowTemplate<Time>(this) {

            @Override
            Time readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getTime(1);
            }

        }.execute(sql);
    }

    public Timestamp getTimestamp(String table, String column) {
        final String sql = "select " + quote(column) + " from " + quote(table);

        return new RowTemplate<Timestamp>(this) {

            @Override
            Timestamp readRow(ResultSet rs, String sql) throws SQLException {
                return rs.getTimestamp(1);
            }

        }.execute(sql);
    }

    public Connection getConnection() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Error opening connection", e);
        }

        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {

            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
        return connection;
    }

}
