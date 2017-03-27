package io.bootique.jdbc.test.csv;

import java.util.Arrays;

import static java.util.Arrays.asList;

public class RowKey {

    private Object[] key;

    RowKey(Object[] key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof RowKey)) {
            return false;
        }

        RowKey anotherKey = (RowKey) object;
        return Arrays.equals(key, anotherKey.key);
    }

    @Override
    public int hashCode() {
        return asList(key).hashCode();
    }


    @Override
    public String toString() {
        return Arrays.toString(key);
    }
}
