package io.bootique.jdbc.hikaricp;


import com.google.inject.Binder;
import com.google.inject.Module;
import io.bootique.jdbc.JdbcModule;

public class HikariCPModule implements Module {

    @Override
    public void configure(Binder binder) {
        JdbcModule.extend(binder).addFactoryType(HikariCPManagedDataSourceFactory.class);
    }
}
