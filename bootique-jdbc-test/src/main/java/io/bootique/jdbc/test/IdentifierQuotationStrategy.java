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
