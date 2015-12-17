package com.nhl.bootique.jdbc;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Provides;
import com.nhl.bootique.ConfigModule;
import com.nhl.bootique.factory.FactoryConfigurationService;

public class JdbcModule extends ConfigModule {

	public JdbcModule() {
	}

	public JdbcModule(String configPrefix) {
		super(configPrefix);
	}

	@Provides
	public DataSourceFactory createDataSource(FactoryConfigurationService configService) {
		Map<String, Map<String, String>> configs = configService
				.factory(new TypeReference<Map<String, Map<String, String>>>() {
				}, configPrefix);
		return new LazyDataSourceFactory(configs);
	}
}
