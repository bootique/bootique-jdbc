package com.nhl.bootique.jdbc;

import static com.codahale.metrics.MetricRegistry.name;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

public class LazyDataSourceFactory implements DataSourceFactory {

	private Map<String, Map<String, String>> configs;
	private ConcurrentMap<String, org.apache.tomcat.jdbc.pool.DataSource> dataSources;
	private MetricRegistry metricRegistry;

	public LazyDataSourceFactory(Map<String, Map<String, String>> configs, MetricRegistry metricRegistry) {
		this.configs = Objects.requireNonNull(configs);
		this.dataSources = new ConcurrentHashMap<>();
		this.metricRegistry = Objects.requireNonNull(metricRegistry);
	}

	void shutdown() {
		dataSources.values().forEach(d -> d.close());
	}

	/**
	 * @since 0.6
	 */
	@Override
	public Collection<String> allNames() {
		return configs.keySet();
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

		ConnectionPool pool;
		try {
			pool = dataSource.createPool();
		} catch (Exception e) {
			throw new RuntimeException("Error creating DataSource", e);
		}

		collectPoolMetrics(name, pool);

		return dataSource;
	}

	void collectPoolMetrics(String name, ConnectionPool pool) {
		metricRegistry.register(name(getClass(), name, "active"), new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return pool.getActive();
			}
		});

		metricRegistry.register(name(getClass(), name, "idle"), new Gauge<Integer>() {

			@Override
			public Integer getValue() {
				return pool.getIdle();
			}
		});

		metricRegistry.register(name(getClass(), name, "waiting"), new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return pool.getWaitCount();
			}
		});

		metricRegistry.register(name(getClass(), name, "size"), new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return pool.getSize();
			}
		});
	}
}
