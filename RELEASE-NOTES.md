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
