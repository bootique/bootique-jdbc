/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.jdbc.test.matcher;

import io.bootique.jdbc.test.Column;
import org.junit.jupiter.api.Assertions;

import java.util.List;

/**
 * A factory of {@link RowKey} objects.
 *
 * @since 2.0
 */
public class RowKeyFactory {

    private int[] keyIndexes;

    RowKeyFactory(int[] keyIndexes) {
        this.keyIndexes = keyIndexes;
    }

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

            Assertions.assertTrue(index >= 0, "Key '" + rowKeyColumns[i] + "' is not a part of the row");
            keyIndexes[i] = index;
        }

        return new RowKeyFactory(keyIndexes);
    }

    public RowKey createKey(Object[] row) {
        Object[] key = new Object[keyIndexes.length];

        for (int i = 0; i < key.length; i++) {
            key[i] = row[keyIndexes[i]];
        }

        return new RowKey(key);
    }
}
