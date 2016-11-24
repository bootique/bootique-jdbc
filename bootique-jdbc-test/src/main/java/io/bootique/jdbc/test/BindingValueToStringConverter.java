package io.bootique.jdbc.test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @since 0.13
 */
public class BindingValueToStringConverter {

    private static final int TRIM_VALUES_THRESHOLD = 30;

    private Map<Class<?>, Function<Object, String>> converters;
    private Function<Object, String> nullConverter;
    private Function<Object, String> defaultConverter;

    public BindingValueToStringConverter() {
        this.nullConverter = o -> "null";
        this.defaultConverter = o -> o.getClass().getName() + "@" + System.identityHashCode(o);

        this.converters = new ConcurrentHashMap<>();

        converters.put(String.class, o -> {

            StringBuilder buffer = new StringBuilder();
            buffer.append('\'');

            // lets escape quotes
            String literal = (String) o;
            if (literal.length() > TRIM_VALUES_THRESHOLD) {
                literal = literal.substring(0, TRIM_VALUES_THRESHOLD) + "...";
            }

            int curPos = 0;
            int endPos;

            while ((endPos = literal.indexOf('\'', curPos)) >= 0) {
                buffer.append(literal.substring(curPos, endPos + 1)).append('\'');
                curPos = endPos + 1;
            }

            if (curPos < literal.length())
                buffer.append(literal.substring(curPos));

            buffer.append('\'');
            return buffer.toString();
        });

        converters.put(Number.class, Object::toString);

        // TODO: add more types...
    }

    protected Function<Object, String> converter(Object value) {
        if (value == null) {
            return nullConverter;
        }

        Class<?> type = value.getClass();
        return converters.computeIfAbsent(type, t -> {
            Function<Object, String> c = null;
            Class<?> st = t.getSuperclass();

            while (c == null && st != Object.class) {
                c = converters.get(st);
                st = st.getSuperclass();
            }

            return c != null ? c : defaultConverter;
        });
    }

    public String convert(Object value) {
        return converter(value).apply(value);
    }
}
