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

import io.bootique.jdbc.junit5.DbTester;
import io.bootique.jdbc.junit5.derby.DerbyTester;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;

// tag::declarations[]
@BQTest // <1>
public class DerbyTest {

    @BQTestTool // <2>
    static final DbTester tester = DerbyTester.db();
}
// end::declarations[]
