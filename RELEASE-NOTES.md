## 4.0-M1

* #137 Remove deprecated modules and APIs
* #138 Liquibase - exclude "javax.xml.bind:jaxb-api" dependency
* #139 Upgrade Apache Derby dependency to 10.17.x.x
* #140 "assertMatchesCsv(..)" can not compare CSVs that store nulls as empty strings

## 3.0-RC1

* #136 Upgrade HikariCP to 6.2.1

## 3.0-M6

* #123 Upgrade to Liquibase 4.x
* #135 Explicitly disable Liquibase analytics

## 3.0-M3

* #132 Deprecate Tomcat DataSource
* #133 Upgrade to Hikari 5.1.0

## 3.0.M2

* #125 upgrade HikariCP to 5.0.1
* #126 HikariCP - support for lazy DataSource creation
* #127 Liquibase - conflict with Jakarta modules
* #129 Support Types.NUMERIC in DefaultFromStringConverter

## 3.0.M1

* #114 JUnit 4 support deprecation
* #115 Table: loss of precision when inserting BigDecimal to Derby
* #116 CsvDataSetBuilder can't handle BigDecimal
* #121 Upgrade Derby to 10.15.2.0
* #122 JUnit 5 connector - unify and expand test SQL conditions builders

## 2.0.B1

* #105 Upgrade Testcontainers to 1.15.0-rc2
* #106 Test API: Table.delete().where(..)
* #107 Test API - better SELECT builder
* #108 MySQL 8 requires special treatment of local timestamp and time
* #109 Junit5 : lazy init of the test DataSource
* #110 DbTester.runScript(..)
* #111 Upgrade Testcontainers to 1.15.1
* #112 Log connection URL
* #113 DbTester - make "initDB" calls additive

## 2.0.M1

* #95 Support for JUnit 5
* #96 DbTester - a JUnit 5 extension to bootstrap test databases, provide integration w/Testcontainers
* #97 DbTester support for Liquibase 
* #98 DbTester: support for custom init function
* #99 DbTester: database reusable between test classes
* #100 DbTester drops empty "derby.log" file even if Derby DB is not in use
* #101 Split jdbc-junit5 into Derby and Testcontainers-specific modules
* #102 TcDbTester: support for explicit container instance

## 1.1

* #37 Hard to trace misconfigured DataSources
* #91 bootique-jdbc-test: TableMatcher.eq doesn't work for nulls
* #92 bootique-jdbc-test: support "in" condition in the matcher 

## 1.0

## 1.0.RC1

* #75 Cleaning up APIs deprecated since <= 0.25
* #76 Update Jackson to 2.9.5
* #78 Hikari pool defaults
* #79 Hikari healthchecks are not loaded in HeartbeatFactory
* #80 Hikari healthchecks are not activated if "jdbc.ds.health" config is absent
* #81 Revisit the names and formats of Hikari health check configuration
* #82 Move generic DataSourceHealthCheck to Tomcat, remove "bootique-jdbc-instrumented"
* #83 Hikari instrumented - wait timer is not registered
* #84 Name Hikari pools after Bootique config names
* #85 Align Hikari metrics names with other Bootique metrics
* #86 Metrics renaming to follow naming convention
* #87 Health check renaming to follow naming convention
* #88 Hikari instrumented: remove circular dependency DataSourceFactory <-> ManagedDataSourceStarter

## 0.25

* #48 Extract Tomcat DataSource integration into a separate module
* #51 Add HikariCP as a separate module
* #56 Make bq-jdbc polymorphic
* #57 Replace modules overriding by listeners
* #62 Auto detection of an available connection pool  
* #66 Rename healthcheck labels
* #69 Add bq-jdbc-hikaricp-instrumented module
* #72 Improve auto detection of an available connection pool
* #74 Upgrade to bootique-modules-parent 0.8 

## 0.24

* #39 Change CSV date time format in tests to follow ISO-8601
* #40 Tests: Support for loading Base64-encoded binary data from .csv
* #41 JdbcTestModule - replace 'contributeDataSourceListeners' with an extender
* #42 Test API: Support for boolean values in CSV
* #43 Test API: Generated SQL needs to print pretty String for byte[] and timestamps
* #44 Test API > insert : support specifying columns and values as one String
* #46 Test API: matcher API, support for key/value matching
* #47 Test API: StatementBuilder API
* #53 Test API > DataSet API

## 0.15

* #32 Upgrade to BQ 0.23 and update test API to match the new Bootique test API
* #33 Support for binding java.time classes when inserting test data via Table
* #36 Tests: Table.update(..) is not fully implemented 

## 0.14

* #27 Upgrade to bootique 0.22 and bootique-metrics 0.9
* #28 bootique-jdbc-test: Per-table identifier quoting strategy
* #29 bootique-jdbc-test: support for loading test data from CSV
* #31 bootique-jdbc-test: API for comparing DB data with CSV

## 0.13

* #6 LazyDataSourceFactory must explicitly declare all config properties
* #17 Table.selectOne ignores all columns but the first one
* #19 Prune partial configs
* #20 Test Table.deleteAll(..) should not declare a checked exception
* #21 Tests: DataChannel to quote SQL identifiers by default
* #22 Tests: DatabaseChannel should log prepared statement bindings
* #23 jdbc-test: a simple data manager to keep table definitions and cleans up the DB before each test
* #24 bootique-jdbc-test: Table.insert explicit binding style
* #25 Suppress alarmist Tomcat DataSource messages from the logs

## 0.12

* #10 Upgrade to Bootique 0.20
* #11 Basic Derby-based unit test framework
* #12 JDBC test utils inspired by Cayenne unit tests
* #13 DataSource healthchecks
* #14 InstrumentedJdbcModule uses incorrect config key
* #15 DataSourceFactory must be a singleton
* #16 Move TestDatabase from @Rule into DI-managed service attached to test runtime

## 0.11

* #8 Upgrade to bootique 0.19 and metrics 0.7 to have access to healthchecks API
* #9 Move to io.bootique namespace

## 0.10

* #4 Split metrics-related logic into a separate bootique-jdbc-instrumented submodule

## 0.9:

* #3 Integrate with bootique-metrics
* #5 Upgrade to Bootique 0.12

## 0.8:

* #2 Adding Shutdown hook

## 0.6:

* #1 DataSourceFactory.allNames() method
