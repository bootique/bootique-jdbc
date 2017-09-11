package io.bootique.jdbc.test;

import io.bootique.BQRuntime;
import io.bootique.test.junit.BQTestFactory;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefaultDatabaseChannelIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();

    private DefaultDatabaseChannel loadChannel(String configResource) {
        BQRuntime runtime = TEST_FACTORY
                .app("-c", configResource)
                .autoLoadModules()
                .createRuntime();

        DatabaseChannel channel = DatabaseChannel.get(runtime);

        assertTrue(channel instanceof DefaultDatabaseChannel);
        return (DefaultDatabaseChannel) channel;
    }

    @Test
    public void testDefaultQuotesOn() {
        DefaultDatabaseChannel channel = loadChannel("classpath:io/bootique/jdbc/test/DefaultDatabaseChannel_QuoteIT.yml");
        assertEquals("\"a\"", channel.getIdentifierQuotationStrategy().quoted("a"));
    }

    @Test
    public void testDefaultQuotesOff() {
        DefaultDatabaseChannel channel = loadChannel("classpath:io/bootique/jdbc/test/DefaultDatabaseChannel_NoQuoteIT.yml");
        assertEquals("a", channel.getIdentifierQuotationStrategy().quoted("a"));
    }
}
