package io.bootique.jdbc;

import javax.sql.DataSource;
import java.util.Collection;

public interface DataSourceFactory {

	DataSource forName(String dataSourceName);

	/**
	 * Returns the names of all configured DataSources. Each of these names can
	 * be used as an argument to {@link #forName(String)} method.
	 * 
	 * @since 0.6
	 * @return the names of all known DataSources.
	 */
	Collection<String> allNames();
}
