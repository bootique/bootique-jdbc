/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.jdbc.test;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.test.junit5.BQApp;
import io.bootique.test.junit5.BQTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BQTest
public class DefaultDatabaseChannelIT {

    @BQApp(skipRun = true)
    static final BQRuntime quotesOn = Bootique
            .app("-c", "classpath:io/bootique/jdbc/test/DefaultDatabaseChannel_QuoteIT.yml")
            .autoLoadModules()
            .createRuntime();

    @BQApp(skipRun = true)
    static final BQRuntime quotesOff = Bootique
            .app("-c", "classpath:io/bootique/jdbc/test/DefaultDatabaseChannel_NoQuoteIT.yml")
            .autoLoadModules()
            .createRuntime();

    @Test
    public void testDefaultQuotesOn() {
        DefaultDatabaseChannel channel = (DefaultDatabaseChannel) DatabaseChannel.get(quotesOn);
        assertEquals("\"a\"", channel.getDefaultIdentifierQuotationStrategy().quoted("a"));
    }

    @Test
    public void testDefaultQuotesOff() {
        DefaultDatabaseChannel channel = (DefaultDatabaseChannel) DatabaseChannel.get(quotesOff);
        assertEquals("a", channel.getDefaultIdentifierQuotationStrategy().quoted("a"));
    }
}
