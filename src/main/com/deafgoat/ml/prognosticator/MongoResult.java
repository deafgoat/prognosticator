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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

// Weka
import weka.classifiers.Classifier;

// Mongo
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Stores model and experiment results to MongoDB.
 */
public class MongoResult extends MongoMgr {
	/**
	 * Reads a stored WEKA classifier model from the database
	 * @param modelName The name of the model to read from the database
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Classifier readModel(String modelName) throws IOException, ClassNotFoundException {
		DBObject query = new BasicDBObject();
		query.put("name", modelName);
		DBObject dbObj = _collection.findOne(query);
		if (dbObj == null) {
			return null;
		}
		ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) dbObj.get("serializedModelBytes"));
		ObjectInputStream ois = new ObjectInputStream(bis);
		return (Classifier) ois.readObject();
	}

	/**
	 * Writes results of experiment to mongoDB
	 * @param config The name of configuration producing this reesult
	 * @param identifier The name of the result to be written
	 * @param result The result to write to the database
	 */
	public void writeExperiment(String config, String identifier, String result) {
		/* create result object to write */
		BasicDBObject dbObj = new BasicDBObject().append("$set", new BasicDBObject().append(identifier, result));
		/* overwrite if it already exists */
		_collection.update(new BasicDBObject().append("name", config), dbObj, true, false);
	}

	/**
	 * Writes a Weka classifier model to the database
	 * @param config Thename of the configuration used
	 * @param model The model to write to the database
	 * @throws IOException If the model can not be written
	 */
	public void writeModel(String config, Classifier model) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(byteStream);
		oos.writeObject(model);
		oos.flush();
		oos.close();
		/* create model object to write */
		DBObject bso = new BasicDBObject().append("$set", new BasicDBObject().append("serializedModelBytes", byteStream.toByteArray()));
		/* overwrite if it already exists */
		_collection.update(new BasicDBObject().append("name", config), bso, true, false);
	}

	/**
	 * Writes prediction to mongoDB
	 * @param config The name of configuration producing this reesult
	 * @param predictions The predictions to be written to the database
	 */
	public void writeResult(String config, ArrayList<HashMap<String, String>> predictions) {
		/* create result object to write */
		BasicDBObject prediction = new BasicDBObject().append("$pushAll", new BasicDBObject("prediction", predictions));
		/* overwrite if it already exists */
		_collection.update(new BasicDBObject().append("name", config), prediction, true, false);
	}
	
	/**
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @throws UnknownHostException If the database host cannot be resolved
	 */
	public MongoResult(String host, Integer port) throws UnknownHostException {
		super(host, port);
	}

	/**
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @param database The name of the database
	 * @throws UnknownHostException If the database host cannot be resolved
	 */
	public MongoResult(String host, Integer port, String database) throws UnknownHostException {
		super(host, port, database);
	}

	/**
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @param database The name of the database
	 * @param collection The name of the collection
	 * @throws UnknownHostException If the database host cannot be resolved
	 */
	public MongoResult(String host, Integer port, String database, String collection) throws UnknownHostException {
		super(host, port, database, collection);
	}
}
