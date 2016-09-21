package io.bootique.jdbc.instrumented;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.LazyDataSourceFactory;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;
import io.bootique.type.TypeRef;

import java.util.Map;

public class InstrumentedJdbcModule extends ConfigModule {

	public InstrumentedJdbcModule() {
		super("jdbc");
	}

	public InstrumentedJdbcModule(String configPrefix) {
		super(configPrefix);
	}

	String getConfixPrefix() {
		return configPrefix;
	}

	@Singleton
	@Provides
	public DataSourceFactory createDataSource(ConfigurationFactory configFactory, BootLogger bootLogger,
											  MetricRegistry metricRegistry, ShutdownManager shutdownManager) {

		Map<String, Map<String, String>> configs = configFactory
				.config(new TypeRef<Map<String,Map<String,String>>>() {
				}, configPrefix);

		LazyDataSourceFactory factory = new InstrumentedLazyDataSourceFactory(configs, metricRegistry);

		shutdownManager.addShutdownHook(() -> {
			bootLogger.trace(() -> "shutting down InstrumentedLazyDataSourceFactory...");
			factory.shutdown();
		});

		return factory;
	}

}
