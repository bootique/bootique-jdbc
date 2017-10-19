[![Build Status](https://travis-ci.org/bootique/bootique-jdbc.svg)](https://travis-ci.org/bootique/bootique-jdbc)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.bootique.jdbc/bootique-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.bootique.jdbc/bootique-jdbc/)

# bootique-jdbc

Provides a number of modules for your [Bootique](http://bootique.io) apps to work with JDBC data stores:

* [bootique-jdbc](https://github.com/bootique/bootique-jdbc) - a module providing injectable configurable map of named JDBC connection pools (`java.sql.DataSource` instances). Uses [Tomcat Connection Pool](https://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html) behind the scenes.
* [bootique-jdbc-instrumented](https://github.com/bootique/bootique-jdbc/tree/master/bootique-jdbc-instrumented) - same as `bootique-jdbc`, but with metrics.
* [bootique-jdbc-test](https://github.com/bootique/bootique-jdbc/tree/master/bootique-jdbc-test) - a DB unit testing facility that helps to prepare test datasets and run assertions against the DB data. Supports API-based and CSV-based data sets. Can be used to test any apps that read or write from/to RDBMS. E.g. Bootique JDBC apps, non-Bootique JDBC apps, [Cayenne apps](https://github.com/bootique/bootique-cayenne/tree/master/bootique-cayenne-test).

See usage example [bootique-jdbc-demo](https://github.com/bootique-examples/bootique-jdbc-demo).