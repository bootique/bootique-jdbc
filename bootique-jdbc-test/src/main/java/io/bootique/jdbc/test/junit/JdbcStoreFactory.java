package io.bootique.jdbc.test.junit;

import io.bootique.jdbc.test.JdbcStore;
import io.bootique.jdbc.test.Table;

/**
 * Created by andrus on 9/17/16.
 */
public interface JdbcStoreFactory {

    JdbcStore getStore();

    Table getTable(String name);
}
