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
package io.bootique.jdbc.liquibase;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.logging.LogType;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.yaml.YamlParser;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

// Ported from LB YamlChangeLogParser, with changes to support the latest SnakeYAML parser API
// TODO: maybe on Liquibase 4.x we no longer need this hack?
class ModernYamlChangeLogParser extends YamlParser implements ChangeLogParser {

    // change priority to override YamlChangeLogParser
    @Override
    public int getPriority() {
        return super.getPriority() + 1;
    }

    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters
            changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {

        // the only line different from LB YamlChangeLogParser
        Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));

        try (InputStream changeLogStream = StreamUtil.singleInputStream(physicalChangeLogLocation, resourceAccessor)) {
            if (changeLogStream == null) {
                throw new ChangeLogParseException(physicalChangeLogLocation + " does not exist");
            }

            Map parsedYaml = parseYamlStream(physicalChangeLogLocation, yaml, changeLogStream);

            if ((parsedYaml == null) || parsedYaml.isEmpty()) {
                throw new ChangeLogParseException("Empty file " + physicalChangeLogLocation);
            }

            DatabaseChangeLog changeLog = new DatabaseChangeLog(physicalChangeLogLocation);

            Object rootList = parsedYaml.get("databaseChangeLog");
            if (rootList == null) {
                throw new ChangeLogParseException("Could not find databaseChangeLog node");
            }

            if (!(rootList instanceof List)) {
                throw new ChangeLogParseException("databaseChangeLog does not contain a list of entries. Each changeSet must begin ' - changeSet:'");
            }

            for (Object obj : (List) rootList) {
                if ((obj instanceof Map) && ((Map) obj).containsKey("property")) {
                    Map property = (Map) ((Map) obj).get("property");
                    ContextExpression context = new ContextExpression((String) property.get("context"));
                    Labels labels = new Labels((String) property.get("labels"));

                    Boolean global = getGlobalParam(property);

                    if (property.containsKey("name")) {
                        Object value = property.get("value");
                        if (value != null) {
                            value = value.toString(); // TODO: not nice...
                        }

                        changeLogParameters.set((String) property.get("name"), (String) value, context, labels, (String) property.get("dbms"), global, changeLog);
                    } else if (property.containsKey("file")) {
                        loadChangeLogParametersFromFile(changeLogParameters, resourceAccessor, changeLog, property,
                                context, labels, global);
                    }
                }
            }


            replaceParameters(parsedYaml, changeLogParameters, changeLog);

            changeLog.setChangeLogParameters(changeLogParameters);
            ParsedNode databaseChangeLogNode = new ParsedNode(null, "databaseChangeLog");
            databaseChangeLogNode.setValue(rootList);

            changeLog.load(databaseChangeLogNode, resourceAccessor);

            return changeLog;
        } catch (ChangeLogParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ChangeLogParseException("Error parsing " + physicalChangeLogLocation, e);
        }
    }

    private Map parseYamlStream(String physicalChangeLogLocation, Yaml yaml, InputStream changeLogStream) throws ChangeLogParseException {
        Map parsedYaml;
        try {
            parsedYaml = (Map) yaml.load(changeLogStream);
        } catch (Exception e) {
            throw new ChangeLogParseException("Syntax error in file " + physicalChangeLogLocation + ": " + e.getMessage(), e);
        }
        return parsedYaml;
    }

    private void loadChangeLogParametersFromFile(ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor, DatabaseChangeLog changeLog, Map property, ContextExpression context, Labels labels, Boolean global) throws
            IOException {
        Properties props = new Properties();
        try (
                InputStream propertiesStream = StreamUtil.singleInputStream(
                        (String) property.get("file"), resourceAccessor)) {

            if (propertiesStream == null) {
                log.info(LogType.LOG, "Could not open properties file " + property.get("file"));
            } else {
                props.load(propertiesStream);

                for (Map.Entry entry : props.entrySet()) {
                    changeLogParameters.set(entry.getKey().toString(), entry.getValue().toString(), context, labels, (String) property.get("dbms"), global, changeLog);
                }
            }
        }
    }

    /**
     * Extract the global parameter from the properties.
     *
     * @param property the map of props
     * @return the global param
     */
    private Boolean getGlobalParam(Map property) {
        Boolean global = null;
        Object globalObj = property.get("global");
        if (globalObj == null) {
            // default behaviour before liquibase 3.4
            global = true;
        } else {
            global = (Boolean) globalObj;
        }
        return global;
    }

    protected void replaceParameters(Object obj, ChangeLogParameters changeLogParameters, DatabaseChangeLog changeLog) {
        if (obj instanceof Map) {
            for (Map.Entry entry : (Set<Map.Entry>) ((Map) obj).entrySet()) {
                if ((entry.getValue() instanceof Map) || (entry.getValue() instanceof Collection)) {
                    replaceParameters(entry.getValue(), changeLogParameters, changeLog);
                } else if (entry.getValue() instanceof String) {
                    entry.setValue(changeLogParameters.expandExpressions((String) entry.getValue(), changeLog));
                }
            }
        } else if (obj instanceof Collection) {
            ListIterator iterator = ((List) obj).listIterator();
            while (iterator.hasNext()) {
                Object child = iterator.next();
                if ((child instanceof Map) || (child instanceof Collection)) {
                    replaceParameters(child, changeLogParameters, changeLog);
                } else if (child instanceof String) {
                    iterator.set(changeLogParameters.expandExpressions((String) child, changeLog));
                }
            }
        }
    }
}
