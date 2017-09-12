package io.bootique.jdbc.test.runtime;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jdbc.DataSourceFactory;

/**
 * @since 0.24
 */
@BQConfig
public class DatabaseChannelFactoryFactory {

    private boolean quoteIdentifiers;

    public DatabaseChannelFactoryFactory() {
        this.quoteIdentifiers = true;
    }

    @BQConfigProperty
    public void setQuoteIdentifiers(boolean quoteIdentifiers) {
        this.quoteIdentifiers = quoteIdentifiers;
    }

    public DatabaseChannelFactory createFactory(DataSourceFactory dataSourceFactory) {
        return new DatabaseChannelFactory(dataSourceFactory, quoteIdentifiers);
    }
}
