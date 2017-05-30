package io.bootique.jdbc.test;

import org.junit.Test;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;

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
