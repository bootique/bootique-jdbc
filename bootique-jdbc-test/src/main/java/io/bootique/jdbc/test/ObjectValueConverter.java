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

package io.bootique.jdbc.test;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Converts java types into proper sql types.
 *
 * @deprecated since 3.0.M1, as we are we phasing out JUnit 4 support in favor of JUnit 5
 */
@Deprecated
public class ObjectValueConverter {

    private Map<Class<?>, Function<Object, Object>> converters;
    private Function<Object, Object> nullConverter;
    private Function<Object, Object> defaultConverter;


    public ObjectValueConverter() {

        this.converters = new ConcurrentHashMap<>();

        this.nullConverter = o -> null;
        this.defaultConverter = o -> o;

        converters.put(LocalDate.class, o -> Date.valueOf((LocalDate) o));
        converters.put(LocalTime.class, o -> Time.valueOf((LocalTime) o));
        converters.put(LocalDateTime.class, o -> Timestamp.valueOf((LocalDateTime) o));
    }

    protected Function<Object, Object> converter(Object value) {
        if (value == null) {
            return nullConverter;
        }

        Class<?> type = value.getClass();
        return converters.computeIfAbsent(type, t -> {
            Function<Object, Object> c = null;
            Class<?> st = t.getSuperclass();

            while (c == null && st != Object.class) {
                c = converters.get(st);
                st = st.getSuperclass();
            }

            return c != null ? c : defaultConverter;
        });
    }

    /**
     * Converts {@link LocalDate}, {@link LocalTime} and {@link LocalDateTime} into proper {@link java.sql.Types},
     * otherwise returns {@code value} itself
     *
     * @param value an object to be converted
     * @return converted {@code value}
     */
    public Object convert(Object value) {
        return converter(value).apply(value);
    }
}
