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
package io.bootique.jdbc.junit5.tc.unit;

import io.bootique.jdbc.junit5.tc.TcDbTester;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.BQTestTool;

@BQTest
public abstract class BasePostgresTest {

    @BQTestTool(BQTestScope.GLOBAL)
    protected static final TcDbTester db = TcDbTester
            .db("jdbc:tc:postgresql:11:///mydb")
            // do not delete "t2", only "t1", "t3" and "t4"... this allows to assert the delete behavior in subclasses
            .deleteBeforeEachTest("t1", "t3", "t4")
            .initDB("classpath:io/bootique/jdbc/junit5/tc/unit/BasePostgresTest.sql");
}
