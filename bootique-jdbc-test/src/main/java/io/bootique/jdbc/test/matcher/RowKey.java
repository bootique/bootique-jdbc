/**
 *  Licensed to ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.jdbc.test.matcher;

import java.util.Arrays;

import static java.util.Arrays.asList;

/**
 * A key used to match rows in a tabular data set.
 */
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
