package com.nhl.bootique.jdbc;

import com.google.inject.Module;
import com.nhl.bootique.BQModuleProvider;

public class JdbcModuleProvider implements BQModuleProvider {

	@Override
	public Module module() {
		return new JdbcModule();
	}
}
