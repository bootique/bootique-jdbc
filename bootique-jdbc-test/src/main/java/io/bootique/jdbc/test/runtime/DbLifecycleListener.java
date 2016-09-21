package io.bootique.jdbc.test.runtime;

/**
 * @since 0.12
 */
public interface DbLifecycleListener {

    void beforeStartup(String jdbcUrl);

    void afterShutdown(String jdbcUrl);
}
