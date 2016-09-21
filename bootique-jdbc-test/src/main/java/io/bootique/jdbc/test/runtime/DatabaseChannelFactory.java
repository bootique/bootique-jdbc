package io.bootique.jdbc.test.runtime;

import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.DefaultDatabaseChannel;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @since 0.12
 */
public class DatabaseChannelFactory {

    private DataSourceFactory dataSourceFactory;
    private ConcurrentMap<String, DatabaseChannel> channels;

    public DatabaseChannelFactory(DataSourceFactory dataSourceFactory) {
        this.channels = new ConcurrentHashMap<>();
        this.dataSourceFactory = dataSourceFactory;
    }

    public DatabaseChannel getChannel() {
       Collection<String> names = dataSourceFactory.allNames();
        if(names.size() == 0) {
            throw new IllegalStateException("No DataSources configured");
        }
        if(names.size() > 1) {
            throw new IllegalStateException("More than one DataSource is configured. Don't know which one is default");
        }

        return channels.computeIfAbsent(names.iterator().next(), name -> createChannel(name));
    }

    public DatabaseChannel getChannel(String dataSourceName) {
        return channels.computeIfAbsent(dataSourceName, name -> createChannel(name));
    }

    protected DatabaseChannel createChannel(String dataSourceName) {
        return new DefaultDatabaseChannel(dataSourceFactory.forName(dataSourceName));
    }
}
