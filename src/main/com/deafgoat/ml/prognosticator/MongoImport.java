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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

// JSON
import org.json.JSONException;
import org.json.JSONObject;

// Mongo
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * Reads configuration files from a MongoDB collection.
 */
public class MongoImport extends MongoMgr {

	/**
	 * Returns all JSON configurations found in the collection.
	 * @return The set of all JSON configurations found. Null if none is found.
	 * @throws JSONException If the configuration can not be converted to a JSONObject
	 */
	public ArrayList<JSONObject> getAllConfigurations() throws JSONException {
		DBCursor cursor = _collection.find();
		ArrayList<JSONObject> configFiles = new ArrayList<JSONObject>();
		try {
			while (cursor.hasNext()) {
				configFiles.add(new JSONObject(cursor.next().toString()));
			}
		} finally {
			cursor.close();
		}
		return configFiles;
	}

	/**
	 * Returns the JSON configuraiton matching the specified configuration name.
	 * @param configName The name of the JSON configuration to find.
	 * @return The JSON configurations found. Null if none is found.
	 * @throws IOException If it can not read configuration file.
	 * @throws FileNotFoundException If it can not find configuration file.
	 * @throws JSONException If the configuration file(s) can not be converted to a JSONObject.
	 * @throws MongoException If it is unable to connect to a running mongod.
	 */
	public JSONObject getConfiguration(String configName) throws FileNotFoundException, IOException, MongoException, JSONException {
		DBObject query = new BasicDBObject();
		query.put("name", configName);
		DBObject result = _collection.findOne(query);
		return result == null ? null : new JSONObject(result.toString());
	}

	/**
	 * Removes a JSON configuraiton matching the specified configuration name.
	 * @param configName The name of the JSON configuration to find.
	 * @return The JSON configurations found. Null if none is found.
	 * @throws IOException If it can not read configuration file.
	 * @throws FileNotFoundException If it can not find configuration file.
	 * @throws JSONException If the configuration file(s) can not be converted to a JSONObject.
	 * @throws MongoException If it is unable to connect to a running mongod.
	 */
	public boolean removeConfiguration(String configName) throws FileNotFoundException, IOException, MongoException, JSONException {
		BasicDBObject document = new BasicDBObject();
		document.put("name", configName);
		WriteResult result = _collection.remove(document, WriteConcern.SAFE);
		return result.getError() == null;
	}
	
	/**
	 * Class constructor
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @throws UnknownHostException If the database host cannot be resolved
	 */
	public MongoImport(String host, Integer port) throws UnknownHostException {
		super(host, port);
	}

	/**
	 * Class constructor
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @param database The name of the database
	 * @throws UnknownHostException
	 */
	public MongoImport(String host, Integer port, String database) throws UnknownHostException {
		super(host, port, database);
	}

	/**
	 * Class constructor
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @param database The name of the database
	 * @param collection The name of the collection
	 * @throws UnknownHostException If the database host cannot be resolved
	 */
	public MongoImport(String host, Integer port, String database, String collection) throws UnknownHostException {
		super(host, port, database, collection);
	}

	/**
	 * the set of configuration files found in the whole collection
	 */
	protected String[]	_configFiles;
}
