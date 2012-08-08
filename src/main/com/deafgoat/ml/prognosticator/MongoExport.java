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
import java.io.FileReader;
import java.io.IOException;

// Apache IO
import org.apache.commons.io.IOUtils;

// JSON
import org.json.JSONException;
import org.json.JSONObject;

// Mongo
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * Simple JSON to MongoDB collection writer.
 */
public class MongoExport extends MongoMgr {
	
	/**
	 * Writes the JSON configuration(s) to a MongoDB collection
	 * @throws FileNotFoundException If the configuration file(s) can not be found
	 * @throws IOException If the configuration file(s) can not be read
	 * @throws JSONException If the configuration file(s) can not be converted to a JSONObject
	 */
	public void writeJSON() throws FileNotFoundException, IOException, JSONException {
		BasicDBObject query = new BasicDBObject();
		for (JSONObject configJSON : _configJSONs) {
			query.put("name", configJSON.getString("name"));
			// so we don't have duplicates
			_collection.update(query, (DBObject) JSON.parse(configJSON.toString()), true, false);
		}
	}
	/**
	 * @param json The set of JSON configuration(s) to write to the database
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @throws IOException If it can not read configuration file(s)
	 * @throws FileNotFoundException If it can not find configuration file(s)
	 * @throws JSONException If the configuration file(s) can not be converted to a JSONObject
	 */
	public MongoExport(JSONObject json[], String host, Integer port) throws FileNotFoundException, IOException, JSONException {
		super(host, port);
		_configJSONs = json.clone();
	}

	/**
	 * @param json The set of JSON configuration(s) to write to the database
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @param database The name of the database
	 * @throws IOException If it can not read configuration file(s)
	 * @throws FileNotFoundException If it can not find configuration file(s)
	 * @throws JSONException If the configuration file(s) can not be converted to a JSONObject
	 */
	public MongoExport(JSONObject json[], String host, Integer port, String database) throws FileNotFoundException, IOException, JSONException {
		super(host, port, database);
		_configJSONs = json.clone();
	}

	/**
	 * @param json The set of JSON configuration(s) to write to the database
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @param database The name of the database
	 * @param collection The name of the collection
	 * @throws IOException If it can not read configuration file(s)
	 * @throws FileNotFoundException If it can not find configuration file(s)
	 * @throws JSONException If the configuration file(s) can not be converted to a JSONObject
	 */
	public MongoExport(JSONObject json[], String host, Integer port, String database, String collection) throws FileNotFoundException, IOException, JSONException {
		super(host, port, database, collection);
		_configJSONs = json.clone();
	}

	/**
	 * @param json The set of JSON configuration(s) to write to the database
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @throws IOException If it can not read configuration file(s)
	 * @throws FileNotFoundException If it can not find configuration file(s)
	 * @throws JSONException If the configuration file(s) can not be converted to a JSONObject
	 */
	public MongoExport(String json[], String host, Integer port) throws FileNotFoundException, IOException, JSONException {
		super(host, port);
		_configJSONs = new JSONObject[json.length];
		for (int i = 0; i < json.length; i++) {
			_configJSONs[i] = new JSONObject(IOUtils.toString(new FileReader(json[i])));
		}
	}

	/**
	 * @param json The set of JSON configuration(s) to write to the database
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @param database The name of the database
	 * @throws IOException If it can not read configuration file(s)
	 * @throws FileNotFoundException If it can not find configuration file(s)
	 * @throws JSONException If the configuration file(s) can not be converted to a JSONObject
	 */
	public MongoExport(String json[], String host, Integer port, String database) throws FileNotFoundException, IOException, JSONException {
		super(host, port, database);
		_configJSONs = new JSONObject[json.length];
		for (int i = 0; i < json.length; i++) {
			_configJSONs[i] = new JSONObject(IOUtils.toString(new FileReader(json[i])));
		}
	}

	/**
	 * @param json The set of JSON configuration(s) to write to the database
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @param database The name of the database
	 * @param collection The name of the collection
	 * @throws IOException If it can not read configuration file(s)
	 * @throws FileNotFoundException If it can not find configuration file(s)
	 * @throws JSONException If the configuration file(s) can not be converted to a JSONObject
	 */
	public MongoExport(String json[], String host, Integer port, String database, String collection) throws FileNotFoundException, IOException, JSONException {
		super(host, port, database, collection);
		_configJSONs = new JSONObject[json.length];
		for (int i = 0; i < json.length; i++) {
			_configJSONs[i] = new JSONObject(IOUtils.toString(new FileReader(json[i])));
		}
	}
	
	/**
	 * holds the configuration JSONobject objecdts
	 */
	private JSONObject[] _configJSONs;
}
