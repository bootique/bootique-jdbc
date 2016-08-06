package io.bootique.jdbc;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;

public class JdbcModuleProvider implements BQModuleProvider {

	@Override
	public Module module() {
		return new JdbcModule();
	}
}
