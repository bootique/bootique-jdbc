package com.nhl.bootique.jdbc.instrumented;

import java.util.Collection;
import java.util.Collections;

import com.google.inject.Module;
import com.nhl.bootique.BQModuleProvider;
import com.nhl.bootique.jdbc.JdbcModule;

public class InstrumentedJdbcModuleProvider implements BQModuleProvider {

	@Override
	public Module module() {
		return new InstrumentedJdbcModule();
	}

	@Override
	public Collection<Class<? extends Module>> overrides() {
		return Collections.singleton(JdbcModule.class);
	}

}
