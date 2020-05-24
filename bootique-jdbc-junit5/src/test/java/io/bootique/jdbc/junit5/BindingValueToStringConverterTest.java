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

package io.bootique.jdbc.junit5;

import io.bootique.jdbc.junit5.connector.BindingValueToStringConverter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BindingValueToStringConverterTest {

    private BindingValueToStringConverter converter = new BindingValueToStringConverter();

    @Test
    public void testConvert_Boolean() {
        assertEquals("true", converter.convert(Boolean.TRUE));
        assertEquals("false", converter.convert(Boolean.FALSE));
    }

    @Test
    public void testConvert_Long() {
        assertEquals("15", converter.convert(new Long(15)));
        assertEquals("1000001", converter.convert(new Long(1_000_001)));
    }

    @Test
    public void testConvert_BigDecimal() {
        assertEquals("15.234", converter.convert(new BigDecimal("15.234")));
        assertEquals("-15.236", converter.convert(new BigDecimal("-15.236")));
    }

    @Test
    public void testConvert_Binary() {
        assertEquals("0506", converter.convert(new byte[]{0x5, 0x6}));
        assertEquals("050605060506050605060506050605...",
                converter.convert(new byte[]{0x5, 0x6, 0x5, 0x6, 0x5, 0x6, 0x5, 0x6, 0x5, 0x6, 0x5, 0x6, 0x5, 0x6, 0x5, 0x6, 0x5, 0x6, 0x5, 0x6}));
    }

    @Test
    public void testConvert_Timestamp() {
        Timestamp ts = Timestamp.valueOf(LocalDateTime.of(2017, 01, 01, 02, 02, 02));
        assertEquals("2017-01-01T02:02:02", converter.convert(ts));
    }

    @Test
    public void testConvert_Date() {
        Date d = Date.valueOf(LocalDate.of(2017, 01, 01));
        assertEquals("2017-01-01", converter.convert(d));
    }

    @Test
    public void testConvert_Time() {
        Time t = Time.valueOf(LocalTime.of(02, 02, 02));
        assertEquals("02:02:02", converter.convert(t));
    }
}
