/**
 *  Licensed to ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

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
        assertEquals("\"a\"", channel.getDefaultIdentifierQuotationStrategy().quoted("a"));
    }

    @Test
    public void testDefaultQuotesOff() {
        DefaultDatabaseChannel channel = loadChannel("classpath:io/bootique/jdbc/test/DefaultDatabaseChannel_NoQuoteIT.yml");
        assertEquals("a", channel.getDefaultIdentifierQuotationStrategy().quoted("a"));
    }
}
