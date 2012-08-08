/**
 * Copyright 2012, Wisdom Omuya.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.deafgoat.ml.prognosticator;

// Java
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

// Mongo
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * Simple MongoDB collection to CSV parser. Does not currently support nested
 * collection objects.
 */
public final class Mongo2CSV extends MongoMgr {

	/**
	 * Helper method to join a list of strings
	 * @param iterable The list/iterable to join
	 * @param separator The delimeter to use for join
	 */
	public static String join(Iterable<? extends Object> iterable, String separator) {
		Iterator<? extends Object> iterator;
		if (iterable == null || (!(iterator = iterable.iterator()).hasNext())) {
			return "";
		}
		StringBuilder oBuilder = new StringBuilder(String.valueOf(iterator.next()));
		while (iterator.hasNext()) {
			oBuilder.append(separator).append(iterator.next());
		}
		return oBuilder.toString();
	}

	/**
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @throws UnknownHostException If the database host cannot be resolved
	 */
	public Mongo2CSV(String host, Integer port) throws UnknownHostException {
		super(host, port);
	}

	/**
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @param db The name of the database
	 * @throws UnknownHostException If the database host cannot be resolved
	 */
	public Mongo2CSV(String host, Integer port, String db) throws UnknownHostException {
		super(host, port, db);
	}

	/**
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @param db The name of the database
	 * @param collection The name of the collection
	 * @throws UnknownHostException If the database host cannot be resolved
	 */
	public Mongo2CSV(String host, Integer port, String db, String collection) throws UnknownHostException {
		super(host, port, db, collection);
	}

	/**
	 * Returns the fields of the CSV file
	 * @return Comma delimited field list
	 */
	public String getFields() {
		String csFields = "";
		for (String s : _fields) {
			csFields += s + _delimeter;
		}
		return csFields;
	}

	/**
	 * Sets the keys in the collection we want to save as CSV fields
	 * @param fields The set of fields to read form the collection
	 */
	public void setFields(String[] fields) {
		// case sensitive, doesn't work with nested fields
		_fields = fields;
	}

	/**
	 * Writes the collectionection to a CSV file using the MongoExport command.
	 * @param filename Th name of the file to write the collectionection to
	 * @throws IOException If unable to write to file
	 * @throws FileNotFoundException If the file is not found
	 */
	public void writeCSV(String filename) throws IOException, FileNotFoundException {
		BufferedWriter out = new BufferedWriter(new FileWriter(filename));
		DBCursor cursor = _collection.find();
		out.write(Mongo2CSV.join(Arrays.asList(_fields), _delimeter) + "\n");
		ArrayList<String> entry = new ArrayList<String>();
		DBObject obj;
		while (cursor.hasNext()) {
			obj = cursor.next();
			for (String field : _fields) {
				entry.add((String) obj.get(field));
			}
			out.write(join(entry, _delimeter) + "\n");
			entry.clear();
		}
		cursor.close();
		out.close();
	}
	
	/**
	 * sets of fields to read
	 */
	private String[] _fields;

	/**
	 * the delimeter in the CSV file
	 */
	private static final String	_delimeter	= ",";

}
