package io.bootique.jdbc.tomcat;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.bootique.BQCoreModule;
import io.bootique.jdbc.JdbcModule;

import java.util.logging.Level;

/**
 * @since 0.25
 */
public class JdbcTomcatModule implements Module {

    @Override
    public void configure(Binder binder) {

        // suppress Tomcat PooledConnection logger. It logs some absolutely benign stuff as WARN
        // per https://github.com/bootique/bootique-jdbc/issues/25

        // it only works partially, namely when SLF intercepts JUL (e.g. under bootique-logback and such).

        // TODO: submit a patch to Tomcat to reduce this log level down...
        BQCoreModule.extend(binder)
                .setLogLevel(org.apache.tomcat.jdbc.pool.PooledConnection.class.getName(), Level.OFF);

        JdbcModule.extend(binder).addFactoryType(TomcatManagedDataSourceFactory.class);
    }
}

