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
package io.bootique.jdbc.junit5.derby;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@BQTest
public class DerbyTester_ExplicitFolderIT extends BaseDerbyTesterTest {

    @TempDir
    static File tempDir;

    @BQTestTool
    final DerbyTester db = DerbyTester.db(tempDir);

    @BQApp(skipRun = true)
    final BQRuntime app = Bootique.app()
            .autoLoadModules()
            .module(db.moduleWithTestDataSource("myDS"))
            .createRuntime();

    @Test
    @Order(0)
    @DisplayName("Derby DataSource must be in use")
    public void testDerby() {
        assertTrue(tempDir.isDirectory());
        assertTrue(new File(tempDir, "derby").isDirectory());
    }
}
