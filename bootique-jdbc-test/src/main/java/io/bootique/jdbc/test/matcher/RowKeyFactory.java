package io.bootique.jdbc.test.matcher;

import io.bootique.jdbc.test.Column;
import org.junit.Assert;

import java.util.List;

/**
 * A factory of {@link RowKey} objects.
 */
public class RowKeyFactory {

    private int[] keyIndexes;

    public static RowKeyFactory create(List<Column> header, String[] rowKeyColumns) {
        int[] keyIndexes = new int[rowKeyColumns.length];

        for (int i = 0; i < rowKeyColumns.length; i++) {

            int index = -1;

            for (int j = 0; j < header.size(); j++) {

                if (rowKeyColumns[i].equals(header.get(j).getName())) {
                    index = j;
                    break;
                }
            }

            Assert.assertTrue("Key '" + rowKeyColumns[i] + "' is not a part of the row", index >= 0);
            keyIndexes[i] = index;
        }

        return new RowKeyFactory(keyIndexes);
    }

    RowKeyFactory(int[] keyIndexes) {
        this.keyIndexes = keyIndexes;
    }

    public RowKey createKey(Object[] row) {
        Object[] key = new Object[keyIndexes.length];

        for (int i = 0; i < key.length; i++) {
            key[i] = row[keyIndexes[i]];
        }

        return new RowKey(key);
    }
}
