package io.bootique.jdbc.test;

import org.junit.Test;

import java.math.BigDecimal;

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
}
