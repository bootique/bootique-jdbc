package io.bootique.jdbc.test;

import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.Assert.assertEquals;

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
