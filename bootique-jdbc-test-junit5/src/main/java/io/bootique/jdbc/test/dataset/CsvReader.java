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

package io.bootique.jdbc.test.dataset;

import io.bootique.jdbc.test.Table;
import io.bootique.jdbc.test.metadata.DbColumnMetadata;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class CsvReader {

    private FromStringConverter valueConverter;
    private Table table;

    CsvReader(Table table, FromStringConverter valueConverter) {
        this.valueConverter = valueConverter;
        this.table = table;
    }

    TableDataSet loadDataSet(Reader dataReader) throws IOException {
        try (CSVParser parser = new CSVParser(dataReader, CSVFormat.DEFAULT, 0, 0)) {

            Iterator<CSVRecord> rows = parser.iterator();
            if (!rows.hasNext()) {
                return new TableDataSet(table, new DbColumnMetadata[0], Collections.emptyList());
            }

           DbColumnMetadata[] header = getHeader(rows.next());
            return readData(header, rows);
        }
    }

    TableDataSet loadDataSet(DbColumnMetadata[] header, Reader dataReader) throws IOException {
        try (CSVParser parser = new CSVParser(dataReader, CSVFormat.DEFAULT, 0, 0)) {
            return readData(header, parser.iterator());
        }
    }

    private DbColumnMetadata[] getHeader(CSVRecord record) {
        DbColumnMetadata[] header = new DbColumnMetadata[record.size()];

        for (int i = 0; i < record.size(); i++) {
            header[i] = table.getMetadata().getColumn(record.get(i));
        }

        return header;
    }

    private Object[] getRow(CSVRecord csvRow, DbColumnMetadata[] header) {

        Object[] row = new Object[csvRow.size()];

        int len = header.length;
        for (int i = 0; i < len; i++) {
            row[i] = valueConverter.fromString(csvRow.get(i), header[i]);
        }

        return row;
    }

    private TableDataSet readData(DbColumnMetadata[] header, Iterator<CSVRecord> rows) {

        List<Object[]> records = new ArrayList<>();
        while (rows.hasNext()) {
            records.add(getRow(rows.next(), header));
        }

        return new TableDataSet(table, header, records);
    }
}
