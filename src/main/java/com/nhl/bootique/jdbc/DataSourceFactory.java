package com.nhl.bootique.jdbc;

import javax.sql.DataSource;

public interface DataSourceFactory {

	DataSource forName(String dataSourceName);
}
