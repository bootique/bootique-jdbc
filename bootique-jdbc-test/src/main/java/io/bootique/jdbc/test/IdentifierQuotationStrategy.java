/**
 *    Licensed to the ObjectStyle LLC under one
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

import java.util.Objects;

/**
 * @since 0.14
 */
public interface IdentifierQuotationStrategy {

    String quoted(String bareIdentifier);

    /**
     * @param quote a quote symbol for identifiers.
     * @return a strategy that will enclose identifiers in the provided quotation symbol.
     * @since 0.24
     */
    static IdentifierQuotationStrategy forQuoteSymbol(String quote) {
        Objects.requireNonNull(quote);
        return id -> quote + id + quote;
    }

    /**
     * @return a strategy that will returns identifiers unchanged.
     * @since 0.24
     */
    static IdentifierQuotationStrategy noQuote() {
        return id -> id;
    }
}
