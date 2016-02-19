package com.nhl.bootique.jdbc.instrumented;

import com.google.inject.Module;
import com.nhl.bootique.BQModuleProvider;

public class InstrumentedJdbcModuleProvider implements BQModuleProvider {

	@Override
	public Module module() {
		return new InstrumentedJdbcModule();
	}

}
