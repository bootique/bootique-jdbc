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

package io.bootique.jdbc.tomcat;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.bootique.BQCoreModule;
import io.bootique.jdbc.JdbcModule;

import java.util.logging.Level;

/**
 * @since 0.25
 */
public class JdbcTomcatModule implements Module {

    @Override
    public void configure(Binder binder) {

        // suppress Tomcat PooledConnection logger. It logs some absolutely benign stuff as WARN
        // per https://github.com/bootique/bootique-jdbc/issues/25

        // it only works partially, namely when SLF intercepts JUL (e.g. under bootique-logback and such).

        // TODO: submit a patch to Tomcat to reduce this log level down...
        BQCoreModule.extend(binder)
                .setLogLevel(org.apache.tomcat.jdbc.pool.PooledConnection.class.getName(), Level.OFF);

        JdbcModule.extend(binder).addFactoryType(TomcatManagedDataSourceFactory.class);
    }
}

