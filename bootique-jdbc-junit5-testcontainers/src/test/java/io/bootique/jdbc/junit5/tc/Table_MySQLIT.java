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

package io.bootique.jdbc.junit5.tc;

import io.bootique.jdbc.junit5.Table;
import io.bootique.jdbc.junit5.tc.unit.BaseMySQLTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Table_MySQLIT extends BaseMySQLTest {

    static final Table t1 = db.getTable("t1");
    static final Table t5 = db.getTable("t5");

    @Test
    public void dateTime_ViaSelect() {
        LocalDateTime ldt = LocalDateTime.of(2018, 1, 10, 4, 0, 1);
        t1.insertColumns("c1", "c2").values(1, ldt).exec();

        t1.matcher().assertOneMatch();
        Object[] data = t1.selectColumns("c2").where("c1", 1).selectOne(null);
        assertEquals(Timestamp.valueOf(ldt), data[0], "Timestamps do not match");
    }

    @Test
    public void dateTime_ViaMatcher() {
        LocalDateTime ldt = LocalDateTime.of(2018, 1, 10, 4, 0, 1);
        t1.insertColumns("c1", "c2").values(1, ldt).exec();

        t1.matcher().assertOneMatch();
        t1.matcher().eq("c1", 1).andEq("c2", ldt).assertOneMatch();
    }

    @Test
    public void insertNumericColumns() throws SQLException {
        t5.insertColumns("id", "c1", "c2")
                .values(1, null, null)
                .values(2, Long.MAX_VALUE - 5, new BigDecimal("2.345"))
                .values(3, 0, BigDecimal.ZERO)
                .exec();
        t5.matcher().assertMatches(3);

        try (Connection c = db.getConnection()) {
            try (Statement s = c.createStatement()) {
                try (ResultSet rs = s.executeQuery("select `id`, `c1`, `c2` from `t5` order by `id`")) {
                    rs.next();
                    assertEquals(1, rs.getInt(1));
                    assertEquals(null, rs.getObject(2));
                    assertEquals(null, rs.getObject(3));

                    rs.next();
                    assertEquals(2, rs.getInt(1));
                    assertEquals(Long.MAX_VALUE - 5, rs.getLong(2));
                    assertEquals(new BigDecimal("2.345"), rs.getBigDecimal(3));

                    rs.next();
                    assertEquals(3, rs.getInt(1));
                    assertEquals(0L, rs.getLong(2));
                    assertEquals(new BigDecimal("0.000"), rs.getBigDecimal(3));
                }
            }
        }
    }

    @Test
    public void matcher_NumericColumns() {
        t5.insertColumns("id", "c1", "c2")
                .values(2, 1234567890L, new BigDecimal("2.345"))
                .exec();

        t5.matcher().assertOneMatch();
        t5.matcher().eq("id", 2).andEq("c1", 1234567890L).andEq("c2", new BigDecimal("2.345")).assertOneMatch();
    }
}
