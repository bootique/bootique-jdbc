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

package io.bootique.jdbc.junit5.sql;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectValueConverterTest {

    private ObjectValueConverter converter = new ObjectValueConverter();

    private static DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    public void testConvert_LocalDate() {
        LocalDate localDate = LocalDate.of(2018, Month.JANUARY, 9);
        assertEquals(Date.valueOf(localDate), converter.convert(localDate));
    }

    @Test
    public void testConvert_LocalTime() {
        LocalTime localTime = LocalTime.of(12, 0);
        assertEquals(Time.valueOf(localTime), converter.convert(localTime));
    }

    @Test
    public void testConvert_LocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.parse("2018-01-10 14:00:01", DATE_TIME_FORMAT);
        assertEquals(Timestamp.valueOf(localDateTime), converter.convert(localDateTime));
    }

    @Test
    public void testConvert_Default() {
        assertEquals("test", converter.convert("test"));
        assertEquals(3, converter.convert(3));
    }

    @Test
    public void testConvert_Null() {
        assertEquals(null, converter.convert(null));
    }
}
