package com.nhl.bootique.jdbc;

import java.util.Map;

import com.google.inject.Provides;
import com.nhl.bootique.ConfigModule;
import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.type.TypeRef;

public class JdbcModule extends ConfigModule {

	public JdbcModule() {
	}

	public JdbcModule(String configPrefix) {
		super(configPrefix);
	}

	@Provides
	public DataSourceFactory createDataSource(ConfigurationFactory configFactory) {
		Map<String, Map<String, String>> configs = configFactory
				.config(new TypeRef<Map<String, Map<String, String>>>() {
				}, configPrefix);
		return new LazyDataSourceFactory(configs);
	}
}
