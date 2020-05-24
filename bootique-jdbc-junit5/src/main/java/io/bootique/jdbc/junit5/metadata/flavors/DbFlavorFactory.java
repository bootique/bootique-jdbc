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
package io.bootique.jdbc.junit5.metadata.flavors;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @since 2.0
 */
public class DbFlavorFactory {

    public static DbFlavor create(DataSource dataSource) {
        try (Connection c = dataSource.getConnection()) {

            DatabaseMetaData jdbcMd = c.getMetaData();
            return createFlavor(jdbcMd);

        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to DB or retrieving DB metadata");
        }
    }

    private static DbFlavor createFlavor(DatabaseMetaData metadata) throws SQLException {

        String dbName = metadata.getDatabaseProductName();
        if (dbName == null) {
            return GenericFlavor.create(metadata);
        }

        // more string matches are available inside Apache Cayenne DB sniffers
        String dbNameUpper = dbName.toUpperCase();
        if (dbNameUpper.contains("MYSQL")) {
            return MySQLFlavor.create(metadata);
        } else if (dbNameUpper.contains("MARIADB")) {
            return MySQLFlavor.create(metadata);
        } else if (dbNameUpper.contains("APACHE DERBY")) {
            return DerbyFlavor.create(metadata);
        } else if (dbNameUpper.contains("POSTGRESQL")) {
            return PostgresFlavor.create(metadata);
        }

        return GenericFlavor.create(metadata);
    }
}
