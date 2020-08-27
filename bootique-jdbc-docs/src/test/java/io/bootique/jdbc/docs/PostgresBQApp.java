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
package io.bootique.jdbc.docs;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jdbc.junit5.DbTester;
import io.bootique.jdbc.junit5.Table;
import io.bootique.jdbc.junit5.tc.TcDbTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;


@BQTest
public class PostgresBQApp {

    @BQTestTool(BQTestScope.GLOBAL)
    static final DbTester tester = TcDbTester.db("jdbc:tc:postgresql:11:///");

    // tag::moduleWithTestDataSource[]
    @BQApp
    static final BQRuntime app = Bootique.app()
            .autoLoadModules()
            .module(tester.moduleWithTestDataSource("mydb")) // <1>
            .createRuntime();
    // end::moduleWithTestDataSource[]

    // tag::getDataSource[]
    @Test
    public void test1() {

        DataSource dataSource = tester.getDataSource();

        // ... use dataSource to connect to DB
    }
    // end::getDataSource[]


    @Test
    public void test2() {

        // tag::getTable[]
        Table myTable = tester.getTable("my_table");
        // end::getTable[]

        // tag::delete[]
        // delete table contents
        myTable.deleteAll();
        // end::delete[]

        // tag::insert[]
        // insert test data
        myTable.insertColumns("id", "c1")
                .values(1, "x1")
                .values(2, "x2")
                .exec();
        // end::insert[]

        // tag::assertOneMatch[]
        // check that data matches expectations
        myTable.matcher().eq("c1", "x1").assertOneMatch();
        // end::assertOneMatch[]
    }
}

