package io.bootique.jdbc.test.runtime;

import io.bootique.jdbc.DataSourceFactory;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.DefaultDatabaseChannel;
import io.bootique.jdbc.test.IdentifierQuotationStrategy;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @since 0.12
 */
public class DatabaseChannelFactory {

    private boolean quotingIdentifiers;
    private DataSourceFactory dataSourceFactory;
    private ConcurrentMap<String, DatabaseChannel> channels;

    public DatabaseChannelFactory(DataSourceFactory dataSourceFactory, boolean quotingIdentifiers) {
        this.channels = new ConcurrentHashMap<>();
        this.dataSourceFactory = dataSourceFactory;
        this.quotingIdentifiers = quotingIdentifiers;
    }

    public DatabaseChannel getChannel() {
        Collection<String> names = dataSourceFactory.allNames();
        if (names.size() == 0) {
            throw new IllegalStateException("No DataSources configured");
        }
        if (names.size() > 1) {
            throw new IllegalStateException("More than one DataSource is configured. Don't know which one is default");
        }

        return channels.computeIfAbsent(names.iterator().next(), name -> createChannel(name));
    }

    public DatabaseChannel getChannel(String dataSourceName) {
        return channels.computeIfAbsent(dataSourceName, name -> createChannel(name));
    }

    protected DatabaseChannel createChannel(String dataSourceName) {
        DataSource dataSource = dataSourceFactory.forName(dataSourceName);


        return new DefaultDatabaseChannel(dataSource, createQuotationStrategy(dataSource));
    }

    private IdentifierQuotationStrategy createQuotationStrategy(DataSource dataSource) {
        if (quotingIdentifiers) {

            String identifierQuote = getIdentifierQuote(dataSource);

            return identifierQuote != null
                    ? id -> identifierQuote + Objects.requireNonNull(id) + identifierQuote
                    : id -> id;
        }

        return id -> id;
    }

    private String getIdentifierQuote(DataSource dataSource) {
        String identifierQuote;
        try (Connection c = dataSource.getConnection()) {
            identifierQuote = c.getMetaData().getIdentifierQuoteString();
        } catch (SQLException e) {
            throw new RuntimeException("Error reading test DB metadata");
        }

        // if no quotations are supported, per JDBC spec the returned value is space,
        // so "trim" should do the trick of converting this to no-quote

        return identifierQuote.trim().length() == 0 ? null : identifierQuote;
    }
}
