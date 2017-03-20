package io.bootique.jdbc.test;

/**
 * @since 0.14
 */
public interface IdentifierQuotationStrategy {

    String quoted(String bareIdentifier);
}
