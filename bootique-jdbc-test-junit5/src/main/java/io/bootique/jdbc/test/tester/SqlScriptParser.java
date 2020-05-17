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
package io.bootique.jdbc.test.tester;

import io.bootique.resource.ResourceFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.0
 */
public class SqlScriptParser {

    private String commentPrefix;
    private String blockCommentStart;
    private String blockCommentEnd;
    private String separator;

    public SqlScriptParser(String commentPrefix, String blockCommentStart, String blockCommentEnd, String separator) {
        this.commentPrefix = commentPrefix;
        this.blockCommentStart = blockCommentStart;
        this.blockCommentEnd = blockCommentEnd;
        this.separator = separator;
    }

    public Iterable<String> getStatements(ResourceFactory source) {
        List<String> statements = new ArrayList<>();
        readStatements(readScript(source), statements);
        return statements;
    }

    protected String readScript(ResourceFactory source) {

        StringBuilder buffer = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(source.getUrl().openStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading init DB script from " + source.getUrl(), e);
        }

        return buffer.toString();
    }

    // adapted from Testcontainers ScriptUtils that was in turn adapted from Spring ScriptUtils
    protected void readStatements(String script, List<String> statements) {

        StringBuilder buffer = new StringBuilder();
        boolean inLiteral = false;
        boolean inEscape = false;
        char[] content = script.toCharArray();
        for (int i = 0; i < script.length(); i++) {
            char c = content[i];
            if (inEscape) {
                inEscape = false;
                buffer.append(c);
                continue;
            }
            // MySQL style escapes
            if (c == '\\') {
                inEscape = true;
                buffer.append(c);
                continue;
            }
            if (c == '\'') {
                inLiteral = !inLiteral;
            }
            if (!inLiteral) {
                if (script.startsWith(separator, i)) {
                    // we've reached the end of the current statement
                    if (buffer.length() > 0) {
                        statements.add(buffer.toString());
                        buffer = new StringBuilder();
                    }
                    i += separator.length() - 1;
                    continue;
                } else if (script.startsWith(commentPrefix, i)) {
                    // skip over any content from the start of the comment to the EOL
                    int indexOfNextNewline = script.indexOf("\n", i);
                    if (indexOfNextNewline > i) {
                        i = indexOfNextNewline;
                        continue;
                    } else {
                        // if there's no EOL, we must be at the end
                        // of the script, so stop here.
                        break;
                    }
                } else if (script.startsWith(blockCommentStart, i)) {
                    // skip over any block comments
                    int indexOfCommentEnd = script.indexOf(blockCommentEnd, i);
                    if (indexOfCommentEnd > i) {
                        i = indexOfCommentEnd + blockCommentEnd.length() - 1;
                        continue;
                    } else {
                        throw new RuntimeException(String.format("Missing block comment end delimiter [%s].", blockCommentEnd));
                    }
                } else if (c == ' ' || c == '\n' || c == '\t') {
                    // avoid multiple adjacent whitespace characters
                    if (buffer.length() > 0 && buffer.charAt(buffer.length() - 1) != ' ') {
                        c = ' ';
                    } else {
                        continue;
                    }
                }
            }
            buffer.append(c);
        }

        String statement = buffer.toString();
        if (!statement.isEmpty()) {
            statements.add(statement);
        }
    }
}
