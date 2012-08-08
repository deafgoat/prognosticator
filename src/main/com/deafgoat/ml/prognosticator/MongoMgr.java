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
import java.net.UnknownHostException;

// Mongo
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Mother manager class for all things Mongo.
 */
public class MongoMgr {

	/**
	 * Closes the database connection
	 */
	public void close() {
		_mongo.close();
	}

	/**
	 * @param collection The collection to work with
	 */
	protected void setCollection(String collection) {
		_collection = _database.getCollection(collection);
	}

	/**
	 * @param database Th database to work with
	 */
	protected void setDatabase(String database) {
		_database = _mongo.getDB(database);
	}

	/** 
	 * @return The current working mongo
	 */
	public Mongo getMongo() {
		return _mongo;
	}

	/**
	 * @return The current working database
	 */
	public DB getDatabase() {
		return _database;
	}
	
	/**
	 * @return The current working collection 
	 */
	public DBCollection getCollection() {
		return _collection;
	}
	
	/**
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @throws UnknownHostException If the database host cannot be resolved
	 * @throws MongoException If it is unable to connect to a running mongod
	 */
	protected void setMongo(String host, Integer port) throws UnknownHostException, MongoException {
		_mongo = new Mongo(host, port);
	}
	

	/**
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @throws UnknownHostException If the database host cannot be resolved
	 */
	public MongoMgr(String host, Integer port) throws UnknownHostException {
		_host = host;
		_port = port;
		setMongo(host, port);
	}

	/**
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @param database The name of the database
	 * @throws UnknownHostException If the database host cannot be resolved
	 */
	public MongoMgr(String host, Integer port, String database) throws UnknownHostException {
		this(host, port);
		setDatabase(database);
	}

	/**
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @param database The name of the database
	 * @param collection The name of the collection
	 * @throws UnknownHostException If the database host cannot be resolved
	 */
	public MongoMgr(String host, Integer port, String database, String collection) throws UnknownHostException {
		this(host, port, database);
		setCollection(collection);
	}

	/**
	 * working collection
	 */
	protected DBCollection	_collection;
	
	/**
	 * working database
	 */
	protected DB _database;
	
	/**
	 * working host
	 */
	protected String _host;
	
	/**
	 * working mongo
	 */
	protected Mongo	_mongo;
	
	/**
	 * working port
	 */
	protected Integer _port;
}
