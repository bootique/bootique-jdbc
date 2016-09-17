package io.bootique.jdbc.test;

import org.slf4j.LoggerFactory;

class Logger {

    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Logger.class);

    static void log(String sql) {
        LOGGER.info(sql);
    }
}
