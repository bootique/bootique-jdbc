package io.bootique.jdbc.tomcat;

import com.google.inject.Binder;
import io.bootique.BQCoreModule;
import io.bootique.ConfigModule;

import java.util.logging.Level;

public class TomcatModule extends ConfigModule {

    public TomcatModule(String configPrefix) {
        super(configPrefix);
    }

    @Override
    public void configure(Binder binder) {
        super.configure(binder);

        // suppress Tomcat PooledConnection logger. It logs some absolutely benign stuff as WARN
        // per https://github.com/bootique/bootique-jdbc/issues/25

        // it only works partially, namely when SLF intercepts JUL (e.g. under bootique-logback and such).

        // TODO: submit a patch to Tomcat to reduce this log level down...
        BQCoreModule.extend(binder)
                .setLogLevel(org.apache.tomcat.jdbc.pool.PooledConnection.class.getName(), Level.OFF);
    }
}

