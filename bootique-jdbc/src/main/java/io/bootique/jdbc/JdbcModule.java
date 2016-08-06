package io.bootique.jdbc;

import com.google.inject.Provides;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;
import io.bootique.type.TypeRef;

import java.util.Map;

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
				.config(new TypeRef<Map<String,Map<String,String>>>() {
				}, configPrefix);

		LazyDataSourceFactory factory = new LazyDataSourceFactory(configs);

		shutdownManager.addShutdownHook(() -> {
			bootLogger.trace(() -> "shutting down LazyDataSourceFactory...");
			factory.shutdown();
		});

		return factory;
	}
}
