/**
 *  Licensed to ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.jdbc.test.jdbc;

import io.bootique.jdbc.test.Binding;
import io.bootique.jdbc.test.BindingValueToStringConverter;
import io.bootique.jdbc.test.Column;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.IdentifierQuotationStrategy;
import io.bootique.jdbc.test.ObjectValueConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines API to build a SQL
 *
 * @since 0.24
 */
public abstract class StatementBuilder<T extends StatementBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatementBuilder.class);

    protected ObjectValueConverter objectValueConverter;
    protected BindingValueToStringConverter valueToStringConverter;
    protected IdentifierQuotationStrategy quotationStrategy;
    protected DatabaseChannel channel;

    protected List<Binding> bindings;
    protected StringBuilder sqlBuffer;

    public StatementBuilder(
            DatabaseChannel channel,
            ObjectValueConverter objectValueConverter,
            BindingValueToStringConverter valueToStringConverter,
            IdentifierQuotationStrategy quotationStrategy) {

        this.channel = channel;
        this.objectValueConverter = objectValueConverter;
        this.quotationStrategy = quotationStrategy;
        this.valueToStringConverter = valueToStringConverter;

        this.bindings = new ArrayList<>();
        this.sqlBuffer = new StringBuilder();
    }

    protected void bind(PreparedStatement statement) {
        for (int i = 0; i < bindings.size(); i++) {
            bindings.get(i).bind(statement, i);
        }
    }

    protected void log(String sql, List<Binding> bindings) {

        if (!LOGGER.isInfoEnabled()) {
            return;
        }

        if (bindings.isEmpty()) {
            LOGGER.info(sql);
            return;
        }

        String toLog = bindings
                .stream()
                .map(b -> b.getColumn().getName() + "->" + valueToStringConverter.convert(b.getValue()))
                .collect(Collectors.joining(", ", sql + " [", "]"));

        LOGGER.info(toLog);
    }

    protected String getSql() {
        return sqlBuffer.toString();
    }

    /**
     * Sets a quotation strategy for identifiers that is different from the default startegy defined in the parent
     * channel.
     *
     * @param quotationStrategy new quotation strategy to override the default.
     * @return this builder instance.
     */
    public T quoteIdentifiersWith(IdentifierQuotationStrategy quotationStrategy) {
        this.quotationStrategy = quotationStrategy;
        return (T) this;
    }

    public T append(String sql) {
        sqlBuffer.append(sql);
        return (T) this;
    }

    public T appendIdentifier(String sqlIdentifier) {
        sqlBuffer.append(quotationStrategy.quoted(sqlIdentifier));
        return (T) this;
    }

    public T appendBinding(String columnName, int valueType, Object value) {
        return appendBinding(new Column(columnName, valueType), value);
    }

    public T appendBinding(Column column, Object value) {
        sqlBuffer.append("?");
        bindings.add(new Binding(column, objectValueConverter.convert(value)));
        return (T) this;
    }
}
