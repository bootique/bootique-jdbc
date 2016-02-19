package com.nhl.bootique.jdbc.instrumented;

import java.util.Map;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Provides;
import com.nhl.bootique.ConfigModule;
import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.jdbc.DataSourceFactory;
import com.nhl.bootique.jdbc.LazyDataSourceFactory;
import com.nhl.bootique.log.BootLogger;
import com.nhl.bootique.shutdown.ShutdownManager;
import com.nhl.bootique.type.TypeRef;

public class InstrumentedJdbcModule extends ConfigModule {

	public InstrumentedJdbcModule() {
	}

	public InstrumentedJdbcModule(String configPrefix) {
		super(configPrefix);
	}

	@Provides
	public DataSourceFactory createDataSource(ConfigurationFactory configFactory, BootLogger bootLogger,
			MetricRegistry metricRegistry, ShutdownManager shutdownManager) {

		Map<String, Map<String, String>> configs = configFactory
				.config(new TypeRef<Map<String, Map<String, String>>>() {
				}, configPrefix);

		LazyDataSourceFactory factory = new InstrumentedLazyDataSourceFactory(configs, metricRegistry);

		shutdownManager.addShutdownHook(() -> {
			bootLogger.trace(() -> "shutting down InstrumentedLazyDataSourceFactory...");
			factory.shutdown();
		});

		return factory;
	}

}
