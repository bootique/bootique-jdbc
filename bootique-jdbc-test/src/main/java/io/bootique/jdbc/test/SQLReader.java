package io.bootique.jdbc.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Parses SQL from a URL source. Expectations for the URL contents:
 * <ul>
 * <li>It has to be UTF-8 encoded.
 * <li>All lines starting with "-- " are treated as comments
 * <li>If a statement separator is supplied, it must be at the end of the line
 * or on its own line.
 * <li>If no separator is supplied, then the entire content body sans comments
 * is treated as a single statement.
 * </ul>
 */
public class SQLReader {

	public static Collection<String> statements(URL sqlSource) throws Exception {
		return statements(sqlSource, null);
	}

	public static Collection<String> statements(URL sqlSource, String separator) throws Exception {

		Collection<String> statements = new ArrayList<String>();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(sqlSource.openStream(), "UTF-8"));) {

			String line;
			StringBuilder statement = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				if (appendLine(statement, line, separator)) {
					statements.add(statement.toString());
					statement = new StringBuilder();
				}
			}

			if (statement.length() > 0) {
				statements.add(statement.toString());
			}
		}

		return statements;
	}

	private static boolean appendLine(StringBuilder statement, String line, String separator) {
		if (line.startsWith("-- ")) {
			return false;
		}

		boolean endOfLine = false;

		line = line.trim();
		if (separator != null && line.endsWith(separator)) {
			line = line.substring(0, line.length() - separator.length());
			endOfLine = true;
		}

		if (line.length() > 0) {
			statement.append('\n').append(line);
		}

		return endOfLine;
	}
}
