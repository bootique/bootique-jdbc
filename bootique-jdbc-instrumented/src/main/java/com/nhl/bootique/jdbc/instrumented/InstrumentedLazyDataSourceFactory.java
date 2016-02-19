package com.nhl.bootique.jdbc.instrumented;

import java.util.Map;

import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.DataSource;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.nhl.bootique.jdbc.LazyDataSourceFactory;

public class InstrumentedLazyDataSourceFactory extends LazyDataSourceFactory {

	private MetricRegistry metricRegistry;

	public InstrumentedLazyDataSourceFactory(Map<String, Map<String, String>> configs, MetricRegistry metricRegistry) {
		super(configs);
		this.metricRegistry = metricRegistry;
	}

	@Override
	protected DataSource createDataSource(String name) {
		DataSource dataSource = super.createDataSource(name);
		collectPoolMetrics(name, dataSource.getPool());
		return dataSource;
	}

	void collectPoolMetrics(String name, ConnectionPool pool) {
		metricRegistry.register(MetricRegistry.name(getClass(), name, "active"), new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return pool.getActive();
			}
		});

		metricRegistry.register(MetricRegistry.name(getClass(), name, "idle"), new Gauge<Integer>() {

			@Override
			public Integer getValue() {
				return pool.getIdle();
			}
		});

		metricRegistry.register(MetricRegistry.name(getClass(), name, "waiting"), new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return pool.getWaitCount();
			}
		});

		metricRegistry.register(MetricRegistry.name(getClass(), name, "size"), new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return pool.getSize();
			}
		});
	}

}
