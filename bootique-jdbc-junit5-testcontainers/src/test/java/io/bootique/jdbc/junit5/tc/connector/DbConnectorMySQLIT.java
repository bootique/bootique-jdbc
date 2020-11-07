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

package io.bootique.jdbc.junit5.tc.connector;

import io.bootique.jdbc.junit5.connector.DbConnector;
import io.bootique.jdbc.junit5.metadata.DbMetadata;
import io.bootique.jdbc.junit5.tc.unit.BaseMySQLTest;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DbConnectorMySQLIT extends BaseMySQLTest {

    @Test
    public void testMySQLQuotes() {
        DataSource dataSource = db.getDataSource();
        DbConnector connector = new DbConnector(dataSource, DbMetadata.create(dataSource));
        assertEquals("`a`", connector.getIdentifierQuoter().quoted("a"));
    }
}
