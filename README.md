[![Build Status](https://travis-ci.org/bootique/bootique-jdbc.svg)](https://travis-ci.org/bootique/bootique-jdbc)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.bootique.jdbc/bootique-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.bootique.jdbc/bootique-jdbc/)

# bootique-jdbc

Provides a number of modules for your [Bootique](http://bootique.io) apps to work with JDBC data stores:

* [bootique-jdbc](https://github.com/bootique/bootique-jdbc) - an abstract module providing injectable configurable map of
named JDBC connection pools (`java.sql.DataSource` instances). Normally you won't be importing this module directly.
It will be added automatically as a transitive dependency of concrete modules. There are currently two choices
shown below - Tomcat and Hikari.

* [bootique-jdbc-tomcat](https://github.com/bootique/bootique-jdbc/tree/master/bootique-jdbc-tomcat) - a concrete
module that provides configurable [Tomcat Connection Pool](https://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html).

* [bootique-jdbc-hikaricp](https://github.com/bootique/bootique-jdbc/tree/master/bootique-jdbc-hikaricp) - a concrete
module that provides configurable [Hikari Connection Pool](https://github.com/brettwooldridge/HikariCP).

* [bootique-jdbc-tomcat-instrumented](https://github.com/bootique/bootique-jdbc/tree/master/bootique-jdbc-tomcat-instrumented) -
a variation of `bootique-jdbc-tomcat` with support for performance metrics.

* [bootique-jdbc-hikaricp-instrumented](https://github.com/bootique/bootique-jdbc/tree/master/bootique-jdbc-hikaricp-instrumented) -
a variation of `bootique-jdbc-hikaricp` with support for performance metrics and health checks.

* [bootique-jdbc-test](https://github.com/bootique/bootique-jdbc/tree/master/bootique-jdbc-test) - a DB unit testing
facility that helps to prepare test datasets and run assertions against the DB data. Supports API-based and CSV-based data sets. Can be used to test any apps that read or write from/to RDBMS. E.g. Bootique JDBC apps, non-Bootique JDBC apps, [Cayenne apps](https://github.com/bootique/bootique-cayenne/tree/master/bootique-cayenne-test).

See usage example [bootique-jdbc-demo](https://github.com/bootique-examples/bootique-jdbc-demo).