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
package io.bootique.jdbc.junit5.tc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlTcDbTesterTest {

    @Test
    public void reusableContainerDbUrl() {
        UrlTcDbTester tester = new UrlTcDbTester("dummy");
        assertEquals("jdbc:tc:postgresql:11:///?TC_REUSABLE=true",
                tester.reusableContainerDbUrl("jdbc:tc:postgresql:11:///"));

        assertEquals("jdbc:tc:postgresql:11:///?TC_REUSABLE=true",
                tester.reusableContainerDbUrl("jdbc:tc:postgresql:11:///?"));

        assertEquals("jdbc:tc:postgresql:11:///?TC_INITSCRIPT=file:/abc&TC_REUSABLE=true",
                tester.reusableContainerDbUrl("jdbc:tc:postgresql:11:///?TC_INITSCRIPT=file:/abc"));

        assertEquals("jdbc:tc:postgresql:11:///?TC_REUSABLE=true",
                tester.reusableContainerDbUrl("jdbc:tc:postgresql:11:///?TC_REUSABLE=true"));

        assertEquals("jdbc:tc:postgresql:11:///?TC_REUSABLE=true",
                tester.reusableContainerDbUrl("jdbc:tc:postgresql:11:///?TC_REUSABLE=false"));

        assertEquals("jdbc:tc:postgresql:11:///?TC_REUSABLE=true&TC_INITSCRIPT=file:/abc&TC_INITFUNCTION=a.C:b",
                tester.reusableContainerDbUrl("jdbc:tc:postgresql:11:///?TC_REUSABLE=false&TC_INITSCRIPT=file:/abc&TC_INITFUNCTION=a.C:b"));

        assertEquals("jdbc:tc:postgresql:11:///?TC_REUSABLE=true&TC_INITSCRIPT=file:/abc&TC_INITFUNCTION=a.C:b&TC_BLA=xyz",
                tester.reusableContainerDbUrl("jdbc:tc:postgresql:11:///?TC_REUSABLE=true&TC_INITSCRIPT=file:/abc&TC_INITFUNCTION=a.C:b&TC_BLA=xyz"));

        assertEquals("jdbc:tc:postgresql:11:///?TC_REUSABLE=true&TC_INITSCRIPT=file:/abc&TC_REUSABLE=true&TC_BLA=xyz",
                tester.reusableContainerDbUrl("jdbc:tc:postgresql:11:///?TC_REUSABLE=true&TC_INITSCRIPT=file:/abc&TC_REUSABLE=false&TC_BLA=xyz"));
    }
}
