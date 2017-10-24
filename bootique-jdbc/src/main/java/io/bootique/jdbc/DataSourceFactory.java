package io.bootique.jdbc;

import java.util.Collection;

public interface DataSourceFactory {

    javax.sql.DataSource forName(String dataSourceName);

    /**
     * Returns the names of all configured DataSources. Each of these names can
     * be used as an argument to {@link #forName(String)} method.
     *
     * @return the names of all known DataSources.
     * @since 0.6
     */
    Collection<String> allNames();
}
