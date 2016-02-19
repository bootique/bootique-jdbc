package com.nhl.bootique.jdbc;

import java.util.Map;

import com.google.inject.Provides;
import com.nhl.bootique.ConfigModule;
import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.log.BootLogger;
import com.nhl.bootique.shutdown.ShutdownManager;
import com.nhl.bootique.type.TypeRef;

public class JdbcModule extends ConfigModule {

	public JdbcModule() {
	}

	public JdbcModule(String configPrefix) {
		super(configPrefix);
	}

	@Provides
	public DataSourceFactory createDataSource(ConfigurationFactory configFactory, BootLogger bootLogger,
			ShutdownManager shutdownManager) {
		Map<String, Map<String, String>> configs = configFactory
				.config(new TypeRef<Map<String, Map<String, String>>>() {
				}, configPrefix);

		LazyDataSourceFactory factory = new LazyDataSourceFactory(configs);

		shutdownManager.addShutdownHook(() -> {
			bootLogger.trace(() -> "shutting down LazyDataSourceFactory...");
			factory.shutdown();
		});

		return factory;
	}
}
