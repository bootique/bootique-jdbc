package com.nhl.bootique.jdbc;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolConfiguration;

public class LazyDataSourceFactory implements DataSourceFactory {

	private Map<String, Map<String, String>> configs;
	private ConcurrentMap<String, DataSource> dataSources;

	public LazyDataSourceFactory(Map<String, Map<String, String>> configs) {
		this.configs = configs;
		this.dataSources = new ConcurrentHashMap<>();
	}

	@Override
	public DataSource forName(String dataSourceName) {
		return dataSources.computeIfAbsent(dataSourceName, name -> createDataSource(name));
	}

	org.apache.tomcat.jdbc.pool.DataSource createDataSource(String name) {
		Map<String, String> config = configs.get(name);
		if (config == null) {
			throw new IllegalStateException("No DataSource config for name '" + name + "'");
		}

		Objects.requireNonNull(config.get("url"), "'url' property should not be null");

		Properties properties = new Properties();
		properties.putAll(config);

		PoolConfiguration poolProperties = org.apache.tomcat.jdbc.pool.DataSourceFactory
				.parsePoolProperties(properties);

		org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);

		try {
			dataSource.createPool();
		} catch (Exception e) {
			throw new RuntimeException("Error creating DataSource", e);
		}

		return dataSource;
	}
}
