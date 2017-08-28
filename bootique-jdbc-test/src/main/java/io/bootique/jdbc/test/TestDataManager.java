package io.bootique.jdbc.test;

import org.junit.rules.ExternalResource;

import java.util.HashMap;
import java.util.Map;

/**
 * A unit test "rule" to manage data sets for unit tests.
 *
 * @since 0.13
 */
public class TestDataManager extends ExternalResource {

    private Table[] tablesInInsertOrder;
    private Map<String, Table> tables;
    private boolean deleteData;


    public TestDataManager(boolean deleteData, Table... tablesInInsertOrder) {
        this.deleteData = deleteData;
        this.tablesInInsertOrder = tablesInInsertOrder != null ? tablesInInsertOrder : new Table[0];

        tables = new HashMap<>();
        for (Table t : this.tablesInInsertOrder) {
            tables.put(t.getName(), t);
        }
    }

    @Override
    protected void before() throws Throwable {
        // do cleanup before the test; leave the data alone *after* - perhaps we want to inspect it manually, etc.
        if (deleteData) {
            deleteData();
        }
    }

    /**
     * Deletes data from the managed tables.
     *
     * @since 0.24
     */
    public void deleteData() {
        for (int i = tablesInInsertOrder.length - 1; i >= 0; i--) {
            tablesInInsertOrder[i].deleteAll();
        }
    }

    public Table getTable(String name) {
        return tables.computeIfAbsent(name, n -> {
            throw new IllegalArgumentException("Unknown table name: " + n);
        });
    }

}
